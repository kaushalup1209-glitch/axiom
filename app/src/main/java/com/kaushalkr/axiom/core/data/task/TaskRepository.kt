package com.kaushalkr.axiom.core.data.task

import androidx.room.withTransaction
import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.core.domain.task.TaskRecurrenceCalculator
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.core.model.TaskDraft
import com.kaushalkr.axiom.core.model.TaskLimits
import com.kaushalkr.axiom.core.model.TaskRecurrence
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate

// ===== SECTION: TASK_COMPLETION_RESULT =====
/** What completing a task changed, so the change can be undone exactly. */
data class TaskCompletion(
    val taskId: Long,
    /** The next occurrence created for a repeating task, or null. */
    val spawnedTaskId: Long?,
)
// ===== END SECTION: TASK_COMPLETION_RESULT =====

/**
 * Single source of truth for tasks. Every write that touches more than one row runs in a Room
 * transaction. Used by the task screens, Home and (later) the AI assistant through
 * [com.kaushalkr.axiom.core.domain.task.TaskActions].
 */
class TaskRepository(
    private val database: AxiomDatabase,
    private val timeProvider: TimeProvider,
) {
    private val dao = database.taskDao()

    // ===== SECTION: TASK_REPOSITORY_READ =====
    val openTasks: Flow<List<Task>> = dao.observeOpen().map { rows -> rows.map { it.toModel() } }.distinctUntilChanged()

    fun completedTasks(limit: Int = COMPLETED_HISTORY_LIMIT): Flow<List<Task>> =
        dao.observeCompleted(limit).map { rows -> rows.map { it.toModel() } }.distinctUntilChanged()

    /** Tasks completed on or after the start of [date] in the current time zone. */
    fun completedSince(date: LocalDate): Flow<List<Task>> {
        val sinceMillis = date.atStartOfDay(timeProvider.zone()).toInstant().toEpochMilli()
        return dao.observeCompletedSince(sinceMillis).map { rows -> rows.map { it.toModel() } }.distinctUntilChanged()
    }

    fun observeTask(id: Long): Flow<Task?> = dao.observe(id).map { it?.toModel() }.distinctUntilChanged()

    suspend fun getTask(id: Long): Task? = dao.get(id)?.toModel()

    val subjects: Flow<List<String>> = dao.observeSubjects().distinctUntilChanged()

    fun today(): LocalDate = Instant.ofEpochMilli(timeProvider.nowMillis()).atZone(timeProvider.zone()).toLocalDate()
    // ===== END SECTION: TASK_REPOSITORY_READ =====

    // ===== SECTION: TASK_REPOSITORY_SAVE =====
    /** Creates a task ([id] = 0) or replaces an existing one. Returns the task id. */
    suspend fun save(id: Long, draft: TaskDraft): Long = database.withTransaction {
        val now = timeProvider.nowMillis()
        val existing = if (id != 0L) dao.get(id)?.task else null
        val cleaned = draft.sanitized()
        val dueDate = cleaned.parsedDueDate
        val entity = TaskEntity(
            id = existing?.id ?: 0L,
            title = cleaned.title,
            description = cleaned.description,
            subject = cleaned.subject,
            priority = cleaned.priority.name,
            dueEpochDay = dueDate?.toEpochDay(),
            dueMinuteOfDay = if (dueDate != null) cleaned.parsedDueTime?.toMinuteOfDay() else null,
            estimatedMinutes = cleaned.estimatedMinutes,
            recurrence = cleaned.recurrence.name,
            completedAtMillis = existing?.completedAtMillis,
            createdAtMillis = existing?.createdAtMillis ?: now,
            updatedAtMillis = now,
            sortOrder = existing?.sortOrder ?: (dao.maxSortOrder() + 1.0),
        )
        val subtasks = cleaned.subtasks.mapIndexed { index, subtask ->
            SubtaskEntity(id = 0L, taskId = entity.id, title = subtask.title, isDone = subtask.isDone, position = index)
        }
        dao.replaceTaskWithSubtasks(entity, subtasks)
    }

    private fun TaskDraft.sanitized(): TaskDraft = copy(
        title = title.trim().take(TaskLimits.TITLE_MAX_LENGTH),
        description = description.trim().take(TaskLimits.DESCRIPTION_MAX_LENGTH),
        subject = subject.trim().take(TaskLimits.SUBJECT_MAX_LENGTH),
        estimatedMinutes = estimatedMinutes?.takeIf { it > 0 }?.coerceAtMost(TaskLimits.MAX_ESTIMATED_MINUTES),
        // A repeating task needs a date to repeat from.
        dueDate = dueDate ?: if (recurrence != TaskRecurrence.NONE) today().toString() else null,
        subtasks = subtasks
            .map { it.copy(title = it.title.trim().take(TaskLimits.SUBTASK_MAX_LENGTH)) }
            .filter { it.title.isNotEmpty() }
            .take(TaskLimits.MAX_SUBTASKS),
    )
    // ===== END SECTION: TASK_REPOSITORY_SAVE =====

    // ===== SECTION: TASK_REPOSITORY_COMPLETE =====
    /**
     * Completes a task. A repeating task also gets its next occurrence (subtasks reset), placed
     * right after it in manual order. Returns null if the task does not exist or is done.
     */
    suspend fun complete(id: Long): TaskCompletion? = database.withTransaction {
        val snapshot = dao.get(id) ?: return@withTransaction null
        if (snapshot.task.completedAtMillis != null) return@withTransaction null
        val now = timeProvider.nowMillis()
        dao.setCompletedAt(id, now, now)

        val recurrence = TaskRecurrence.entries.firstOrNull { it.name == snapshot.task.recurrence } ?: TaskRecurrence.NONE
        val nextDate = TaskRecurrenceCalculator.nextDueDate(
            recurrence = recurrence,
            dueDate = snapshot.task.dueEpochDay?.let(LocalDate::ofEpochDay),
            today = today(),
        )
        val spawnedId = if (nextDate != null) {
            val nextTask = snapshot.task.copy(
                id = 0L,
                dueEpochDay = nextDate.toEpochDay(),
                completedAtMillis = null,
                createdAtMillis = now,
                updatedAtMillis = now,
                sortOrder = snapshot.task.sortOrder + NEXT_OCCURRENCE_ORDER_OFFSET,
            )
            dao.replaceTaskWithSubtasks(nextTask, snapshot.subtasks.map { it.copy(id = 0L, isDone = false) })
        } else {
            null
        }
        TaskCompletion(taskId = id, spawnedTaskId = spawnedId)
    }

    /** Reverses [complete]: reopens the task and removes the occurrence it created. */
    suspend fun undoCompletion(completion: TaskCompletion) = database.withTransaction {
        completion.spawnedTaskId?.let { dao.deleteTask(it) }
        dao.setCompletedAt(completion.taskId, null, timeProvider.nowMillis())
    }

    /** Reopens a completed task without touching later occurrences. */
    suspend fun reopen(id: Long) {
        dao.setCompletedAt(id, null, timeProvider.nowMillis())
    }
    // ===== END SECTION: TASK_REPOSITORY_COMPLETE =====

    // ===== SECTION: TASK_REPOSITORY_DELETE =====
    /** Deletes a task and its subtasks. Returns a snapshot that [restore] puts back exactly. */
    suspend fun delete(id: Long): TaskWithSubtasks? = database.withTransaction {
        val snapshot = dao.get(id) ?: return@withTransaction null
        dao.deleteTask(id)
        snapshot
    }

    suspend fun restore(snapshot: TaskWithSubtasks) {
        dao.restore(snapshot)
    }
    // ===== END SECTION: TASK_REPOSITORY_DELETE =====

    // ===== SECTION: TASK_REPOSITORY_EDIT =====
    suspend fun setSubtaskDone(subtaskId: Long, isDone: Boolean) {
        dao.setSubtaskDone(subtaskId, isDone)
    }

    /**
     * Places a task between two neighbours in manual order. Pass null for a missing neighbour
     * (start or end of the list). Only the moved row is written.
     */
    suspend fun move(id: Long, previousSortOrder: Double?, nextSortOrder: Double?) {
        val order = when {
            previousSortOrder != null && nextSortOrder != null -> (previousSortOrder + nextSortOrder) / 2.0
            previousSortOrder != null -> previousSortOrder + 1.0
            nextSortOrder != null -> nextSortOrder - 1.0
            else -> return
        }
        dao.setSortOrder(id, order, timeProvider.nowMillis())
    }
    // ===== END SECTION: TASK_REPOSITORY_EDIT =====

    companion object {
        const val COMPLETED_HISTORY_LIMIT = 300
        private const val NEXT_OCCURRENCE_ORDER_OFFSET = 0.001
    }
}
