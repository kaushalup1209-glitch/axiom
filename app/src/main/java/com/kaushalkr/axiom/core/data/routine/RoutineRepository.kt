package com.kaushalkr.axiom.core.data.routine

import androidx.room.withTransaction
import com.kaushalkr.axiom.core.data.academic.toRef
import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineBlock
import com.kaushalkr.axiom.core.model.RoutineBlockDraft
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.core.model.Weekdays
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import java.time.LocalDate

// ===== SECTION: ROUTINE_SNAPSHOT =====
/** A deleted block with its task links, for exact undo. */
data class RoutineBlockSnapshot(
    val block: RoutineBlockEntity,
    val taskIds: List<Long>,
)
// ===== END SECTION: ROUTINE_SNAPSHOT =====

/**
 * Timetable blocks. The table is small (tens of rows), so every view reads the full list and
 * builds days, weeks and months in memory with [com.kaushalkr.axiom.core.domain.routine.RoutineSchedule].
 */
class RoutineRepository(
    private val database: AxiomDatabase,
    private val timeProvider: TimeProvider,
) {
    private val dao = database.routineDao()
    private val subjectDao = database.subjectDao()

    // ===== SECTION: ROUTINE_REPOSITORY_READ =====
    val blocks: Flow<List<RoutineBlock>> = combine(
        dao.observeBlocks(),
        dao.observeLinkedTasks(),
        subjectDao.observeAll(),
    ) { blocks, links, subjects ->
        val refs = subjects.associate { it.id to it.toRef() }
        val linksByBlock = links.groupBy { it.blockId }
        blocks.map { it.toModel(refs, linksByBlock[it.id].orEmpty()) }
    }.distinctUntilChanged()

    suspend fun getBlock(id: Long): RoutineBlock? {
        val entity = dao.get(id) ?: return null
        val subject = entity.subjectId?.let { subjectDao.get(it) }
        val linkedIds = dao.linkedTaskIds(id).toSet()
        val links = database.taskDao().let { taskDao ->
            linkedIds.mapNotNull { taskId -> taskDao.get(taskId)?.task }
                .map { LinkedTaskRow(blockId = id, taskId = it.id, title = it.title, completedAtMillis = it.completedAtMillis) }
        }
        return entity.toModel(subject?.let { mapOf(it.id to it.toRef()) }.orEmpty(), links)
    }

    fun today(): LocalDate = Instant.ofEpochMilli(timeProvider.nowMillis()).atZone(timeProvider.zone()).toLocalDate()
    // ===== END SECTION: ROUTINE_REPOSITORY_READ =====

    // ===== SECTION: ROUTINE_REPOSITORY_WRITE =====
    /** Creates ([id] = 0) or replaces a block with its task links. Returns the block id. */
    suspend fun save(id: Long, draft: RoutineBlockDraft): Long = database.withTransaction {
        val now = timeProvider.nowMillis()
        val existing = if (id != 0L) dao.get(id) else null
        val mask = draft.daysMask and Weekdays.EVERY_DAY
        val oneOffDate = if (mask == Weekdays.NONE) (draft.parsedDate ?: today()) else null
        val subjectId = draft.subjectId?.takeIf { subjectDao.get(it) != null }
        val entity = RoutineBlockEntity(
            id = existing?.id ?: 0L,
            title = draft.title.trim().take(RoutineLimits.TITLE_MAX_LENGTH),
            kind = draft.kind.name,
            subjectId = subjectId,
            startMinute = draft.startMinute.coerceIn(0, MINUTES_PER_DAY - 1),
            durationMinutes = draft.durationMinutes.coerceIn(RoutineLimits.MIN_DURATION_MINUTES, RoutineLimits.MAX_DURATION_MINUTES),
            daysMask = mask,
            epochDay = oneOffDate?.toEpochDay(),
            notes = draft.notes.trim().take(RoutineLimits.NOTES_MAX_LENGTH),
            autoStartFocus = draft.automation.autoStartFocus,
            trackDistractions = draft.automation.trackDistractions,
            restrictApps = draft.automation.restrictApps,
            createdAtMillis = existing?.createdAtMillis ?: now,
            updatedAtMillis = now,
        )
        dao.replace(entity, draft.linkedTaskIds.distinct().take(RoutineLimits.MAX_LINKED_TASKS))
    }

    /** Moves a block (every occurrence) to a new start; returns the previous start, or null. */
    suspend fun move(id: Long, startMinute: Int): Int? = database.withTransaction {
        val block = dao.get(id) ?: return@withTransaction null
        dao.setStart(id, startMinute.coerceIn(0, MINUTES_PER_DAY - 1), timeProvider.nowMillis())
        block.startMinute
    }

    suspend fun delete(id: Long): RoutineBlockSnapshot? = database.withTransaction {
        val block = dao.get(id) ?: return@withTransaction null
        val snapshot = RoutineBlockSnapshot(block, dao.linkedTaskIds(id))
        dao.delete(id)
        snapshot
    }

    /** Puts back a deleted block; its subject link is dropped if that subject no longer exists. */
    suspend fun restore(snapshot: RoutineBlockSnapshot) {
        val subjectId = snapshot.block.subjectId?.takeIf { subjectDao.get(it) != null }
        dao.restore(snapshot.block.copy(subjectId = subjectId), snapshot.taskIds)
    }
    // ===== END SECTION: ROUTINE_REPOSITORY_WRITE =====
}
