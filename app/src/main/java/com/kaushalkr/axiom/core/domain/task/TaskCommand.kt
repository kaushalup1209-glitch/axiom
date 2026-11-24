package com.kaushalkr.axiom.core.domain.task

import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.model.TaskDraft
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

// ===== SECTION: TASK_COMMAND =====
/**
 * Structured task operations for the AI assistant (Phase 11). The assistant produces these as
 * JSON; the app can show them for confirmation, then run them with [TaskCommandExecutor] through
 * the same code paths the UI uses. Plain data only, no Android types.
 */
@Serializable
sealed interface TaskCommand {

    /** True for changes the user must confirm before they are applied. */
    val needsConfirmation: Boolean

    @Serializable
    @SerialName("create_task")
    data class Create(val draft: TaskDraft) : TaskCommand {
        override val needsConfirmation: Boolean get() = false
    }

    @Serializable
    @SerialName("update_task")
    data class Update(val taskId: Long, val draft: TaskDraft) : TaskCommand {
        override val needsConfirmation: Boolean get() = true
    }

    @Serializable
    @SerialName("reschedule_task")
    data class Reschedule(val taskId: Long, val dueDate: String?, val dueTime: String? = null) : TaskCommand {
        override val needsConfirmation: Boolean get() = true
    }

    @Serializable
    @SerialName("complete_task")
    data class Complete(val taskId: Long) : TaskCommand {
        override val needsConfirmation: Boolean get() = false
    }

    @Serializable
    @SerialName("delete_task")
    data class Delete(val taskId: Long) : TaskCommand {
        override val needsConfirmation: Boolean get() = true
    }

    @Serializable
    @SerialName("list_open_tasks")
    data object ListOpen : TaskCommand {
        override val needsConfirmation: Boolean get() = false
    }
}

@Serializable
sealed interface TaskCommandResult {
    @Serializable
    data class Saved(val taskId: Long) : TaskCommandResult

    @Serializable
    data class Completed(val taskId: Long, val nextOccurrenceId: Long?) : TaskCommandResult

    @Serializable
    data class Deleted(val taskId: Long) : TaskCommandResult

    @Serializable
    data class OpenTasks(val tasks: List<TaskSummary>) : TaskCommandResult

    @Serializable
    data class NotFound(val taskId: Long) : TaskCommandResult

    @Serializable
    data class Invalid(val reason: String) : TaskCommandResult
}

/** Compact task description for an assistant's context window. */
@Serializable
data class TaskSummary(
    val id: Long,
    val title: String,
    val subject: String,
    val priority: String,
    val dueDate: String?,
    val dueTime: String?,
    val estimatedMinutes: Int?,
    val recurrence: String,
    val subtasksDone: Int,
    val subtasksTotal: Int,
)
// ===== END SECTION: TASK_COMMAND =====

// ===== SECTION: TASK_COMMAND_EXECUTOR =====
/** Validates and runs [TaskCommand]s. Undo is offered for completions and deletions. */
class TaskCommandExecutor(
    private val repository: TaskRepository,
    private val actions: TaskActions,
) {
    suspend fun execute(command: TaskCommand): TaskCommandResult = when (command) {
        is TaskCommand.Create -> validate(command.draft) ?: TaskCommandResult.Saved(actions.save(0L, command.draft))
        is TaskCommand.Update -> when {
            repository.getTask(command.taskId) == null -> TaskCommandResult.NotFound(command.taskId)
            else -> validate(command.draft) ?: TaskCommandResult.Saved(actions.save(command.taskId, command.draft))
        }
        is TaskCommand.Reschedule -> reschedule(command)
        is TaskCommand.Complete -> {
            val task = repository.getTask(command.taskId)
            val completion = task?.let { actions.completeNow(it.id, it.title, offerUndo = true) }
            if (completion == null) TaskCommandResult.NotFound(command.taskId)
            else TaskCommandResult.Completed(completion.taskId, completion.spawnedTaskId)
        }
        is TaskCommand.Delete -> {
            val task = repository.getTask(command.taskId)
            if (task != null && actions.deleteNow(task.id, task.title, offerUndo = true)) TaskCommandResult.Deleted(task.id)
            else TaskCommandResult.NotFound(command.taskId)
        }
        TaskCommand.ListOpen -> TaskCommandResult.OpenTasks(
            repository.openTasks.first().map { task ->
                TaskSummary(
                    id = task.id,
                    title = task.title,
                    subject = task.subject,
                    priority = task.priority.name,
                    dueDate = task.dueDate?.toString(),
                    dueTime = task.dueTime?.toString(),
                    estimatedMinutes = task.estimatedMinutes,
                    recurrence = task.recurrence.name,
                    subtasksDone = task.completedSubtasks,
                    subtasksTotal = task.subtasks.size,
                )
            },
        )
    }

    private suspend fun reschedule(command: TaskCommand.Reschedule): TaskCommandResult {
        val task = repository.getTask(command.taskId) ?: return TaskCommandResult.NotFound(command.taskId)
        val draft = task.toDraft().copy(dueDate = command.dueDate, dueTime = command.dueTime)
        return validate(draft) ?: TaskCommandResult.Saved(actions.save(task.id, draft))
    }

    private fun validate(draft: TaskDraft): TaskCommandResult.Invalid? = when {
        draft.title.isBlank() -> TaskCommandResult.Invalid("title is required")
        draft.dueDate != null && runCatching { LocalDate.parse(draft.dueDate) }.isFailure ->
            TaskCommandResult.Invalid("dueDate must be yyyy-MM-dd")
        draft.dueTime != null && runCatching { draft.parsedDueTime }.isFailure ->
            TaskCommandResult.Invalid("dueTime must be HH:mm")
        else -> null
    }
}
// ===== END SECTION: TASK_COMMAND_EXECUTOR =====
