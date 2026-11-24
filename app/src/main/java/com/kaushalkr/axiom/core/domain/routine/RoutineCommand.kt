package com.kaushalkr.axiom.core.domain.routine

import com.kaushalkr.axiom.core.data.academic.ExamRepository
import com.kaushalkr.axiom.core.data.academic.SubjectRepository
import com.kaushalkr.axiom.core.data.routine.RoutineRepository
import com.kaushalkr.axiom.core.model.ExamDraft
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineBlock
import com.kaushalkr.axiom.core.model.RoutineBlockDraft
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.core.model.Weekdays
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

// ===== SECTION: ROUTINE_COMMAND =====
/**
 * Structured timetable and academic operations for the AI assistant (Phase 11). The assistant
 * produces these as JSON; the app previews them, asks for confirmation where
 * [needsConfirmation] is true, then runs them with [RoutineCommandExecutor] through the same
 * repositories the screens use. Overlaps are reported instead of silently saved unless the
 * command explicitly allows them.
 */
@Serializable
sealed interface RoutineCommand {

    val needsConfirmation: Boolean

    @Serializable
    @SerialName("create_routine_block")
    data class CreateBlock(val draft: RoutineBlockDraft, val allowOverlap: Boolean = false) : RoutineCommand {
        override val needsConfirmation: Boolean get() = allowOverlap
    }

    @Serializable
    @SerialName("update_routine_block")
    data class UpdateBlock(val blockId: Long, val draft: RoutineBlockDraft, val allowOverlap: Boolean = false) : RoutineCommand {
        override val needsConfirmation: Boolean get() = true
    }

    @Serializable
    @SerialName("reschedule_routine_block")
    data class RescheduleBlock(
        val blockId: Long,
        /** New start, HH:mm. */
        val startTime: String,
        val durationMinutes: Int? = null,
        val allowOverlap: Boolean = false,
    ) : RoutineCommand {
        override val needsConfirmation: Boolean get() = true
    }

    @Serializable
    @SerialName("delete_routine_block")
    data class DeleteBlock(val blockId: Long) : RoutineCommand {
        override val needsConfirmation: Boolean get() = true
    }

    @Serializable
    @SerialName("list_routine")
    data class ListRoutine(
        /** First day, yyyy-MM-dd. */
        val date: String,
        val days: Int = 1,
    ) : RoutineCommand {
        override val needsConfirmation: Boolean get() = false
    }

    @Serializable
    @SerialName("find_free_time")
    data class FindFreeTime(
        val date: String,
        val durationMinutes: Int,
        val earliest: String = "06:00",
        val latest: String = "23:00",
    ) : RoutineCommand {
        override val needsConfirmation: Boolean get() = false
    }

    @Serializable
    @SerialName("create_subject")
    data class CreateSubject(val name: String, val chapters: List<String> = emptyList()) : RoutineCommand {
        override val needsConfirmation: Boolean get() = false
    }

    @Serializable
    @SerialName("add_chapters")
    data class AddChapters(val subjectId: Long, val chapters: List<String>) : RoutineCommand {
        override val needsConfirmation: Boolean get() = false
    }

    @Serializable
    @SerialName("create_exam")
    data class CreateExam(val draft: ExamDraft) : RoutineCommand {
        override val needsConfirmation: Boolean get() = false
    }
}

@Serializable
sealed interface RoutineCommandResult {
    @Serializable
    data class Saved(val id: Long) : RoutineCommandResult

    @Serializable
    data class Overlaps(val conflicts: List<RoutineConflictSummary>, val nextFreeStart: String?) : RoutineCommandResult

    @Serializable
    data class Deleted(val id: Long) : RoutineCommandResult

    @Serializable
    data class Routine(val days: List<RoutineDaySummary>) : RoutineCommandResult

    @Serializable
    data class FreeTime(val date: String, val slots: List<TimeSlotSummary>) : RoutineCommandResult

    @Serializable
    data class ChaptersAdded(val subjectId: Long, val count: Int) : RoutineCommandResult

    @Serializable
    data class NotFound(val id: Long) : RoutineCommandResult

    @Serializable
    data class Invalid(val reason: String) : RoutineCommandResult
}

/** Compact descriptions for an assistant's context window. */
@Serializable
data class RoutineBlockSummary(
    val id: Long,
    val title: String,
    val kind: String,
    val subject: String?,
    val start: String,
    val end: String,
    val days: List<Int>,
    val date: String?,
    val linkedTaskIds: List<Long>,
)

@Serializable
data class RoutineDaySummary(val date: String, val blocks: List<RoutineBlockSummary>)

@Serializable
data class TimeSlotSummary(val start: String, val end: String, val minutes: Int)

@Serializable
data class RoutineConflictSummary(val blockId: Long, val title: String, val date: String, val start: String, val end: String)
// ===== END SECTION: ROUTINE_COMMAND =====

// ===== SECTION: ROUTINE_COMMAND_EXECUTOR =====
/** Validates and runs [RoutineCommand]s. */
class RoutineCommandExecutor(
    private val routines: RoutineRepository,
    private val subjects: SubjectRepository,
    private val exams: ExamRepository,
    private val actions: RoutineActions,
) {
    suspend fun execute(command: RoutineCommand): RoutineCommandResult = when (command) {
        is RoutineCommand.CreateBlock -> saveBlock(0L, command.draft, command.allowOverlap)
        is RoutineCommand.UpdateBlock ->
            if (routines.getBlock(command.blockId) == null) RoutineCommandResult.NotFound(command.blockId)
            else saveBlock(command.blockId, command.draft, command.allowOverlap)
        is RoutineCommand.RescheduleBlock -> reschedule(command)
        is RoutineCommand.DeleteBlock -> {
            val block = routines.getBlock(command.blockId)
            if (block != null && actions.deleteNow(block.id, block.title, offerUndo = true)) RoutineCommandResult.Deleted(block.id)
            else RoutineCommandResult.NotFound(command.blockId)
        }
        is RoutineCommand.ListRoutine -> listRoutine(command)
        is RoutineCommand.FindFreeTime -> findFreeTime(command)
        is RoutineCommand.CreateSubject -> {
            val id = subjects.create(command.name)
            if (id == null) {
                RoutineCommandResult.Invalid("name is required")
            } else {
                if (command.chapters.isNotEmpty()) subjects.addChapters(id, command.chapters)
                RoutineCommandResult.Saved(id)
            }
        }
        is RoutineCommand.AddChapters ->
            if (subjects.getSubject(command.subjectId) == null) RoutineCommandResult.NotFound(command.subjectId)
            else RoutineCommandResult.ChaptersAdded(command.subjectId, subjects.addChapters(command.subjectId, command.chapters))
        is RoutineCommand.CreateExam -> when {
            command.draft.title.isBlank() -> RoutineCommandResult.Invalid("title is required")
            runCatching { command.draft.parsedDate }.isFailure -> RoutineCommandResult.Invalid("date must be yyyy-MM-dd")
            runCatching { command.draft.parsedTime }.isFailure -> RoutineCommandResult.Invalid("time must be HH:mm")
            else -> RoutineCommandResult.Saved(exams.save(0L, command.draft))
        }
    }

    // ===== SECTION: ROUTINE_COMMAND_EXECUTOR_BLOCKS =====
    private suspend fun saveBlock(id: Long, draft: RoutineBlockDraft, allowOverlap: Boolean): RoutineCommandResult {
        validate(draft)?.let { return it }
        val candidate = draft.toCandidate(id, routines.today())
        if (!allowOverlap) {
            val all = routines.blocks.first()
            val conflicts = RoutineConflictDetector.conflicts(candidate, all, routines.today())
            if (conflicts.isNotEmpty()) {
                val next = RoutineConflictDetector.nextFreeStart(candidate, all, routines.today())
                return RoutineCommandResult.Overlaps(conflicts.map { it.toSummary() }, next?.let(::formatMinute))
            }
        }
        return RoutineCommandResult.Saved(actions.save(id, draft))
    }

    private suspend fun reschedule(command: RoutineCommand.RescheduleBlock): RoutineCommandResult {
        val block = routines.getBlock(command.blockId) ?: return RoutineCommandResult.NotFound(command.blockId)
        if (runCatching { LocalTime.parse(command.startTime) }.isFailure) return RoutineCommandResult.Invalid("startTime must be HH:mm")
        val draft = block.toDraft().copy(
            startTime = command.startTime,
            durationMinutes = command.durationMinutes ?: block.durationMinutes,
        )
        return saveBlock(block.id, draft, command.allowOverlap)
    }

    private suspend fun listRoutine(command: RoutineCommand.ListRoutine): RoutineCommandResult {
        val start = runCatching { LocalDate.parse(command.date) }.getOrNull() ?: return RoutineCommandResult.Invalid("date must be yyyy-MM-dd")
        val all = routines.blocks.first()
        val days = (0 until command.days.coerceIn(1, MAX_LIST_DAYS)).map { offset ->
            val date = start.plusDays(offset.toLong())
            RoutineDaySummary(
                date = date.toString(),
                blocks = RoutineSchedule.occurrencesOn(all, date).filter { !it.isContinuation }.map { it.block.toSummary() },
            )
        }
        return RoutineCommandResult.Routine(days)
    }

    private suspend fun findFreeTime(command: RoutineCommand.FindFreeTime): RoutineCommandResult {
        val date = runCatching { LocalDate.parse(command.date) }.getOrNull() ?: return RoutineCommandResult.Invalid("date must be yyyy-MM-dd")
        val earliest = runCatching { LocalTime.parse(command.earliest) }.getOrNull() ?: return RoutineCommandResult.Invalid("earliest must be HH:mm")
        val latest = runCatching { LocalTime.parse(command.latest) }.getOrNull() ?: return RoutineCommandResult.Invalid("latest must be HH:mm")
        val slots = RoutineSchedule.freeSlots(
            blocks = routines.blocks.first(),
            date = date,
            minDurationMinutes = command.durationMinutes.coerceAtLeast(RoutineLimits.MIN_DURATION_MINUTES),
            fromMinute = earliest.hour * 60 + earliest.minute,
            toMinute = latest.hour * 60 + latest.minute,
        )
        return RoutineCommandResult.FreeTime(
            date = date.toString(),
            slots = slots.map { TimeSlotSummary(formatMinute(it.startMinute), formatMinute(it.endMinute), it.durationMinutes) },
        )
    }

    private fun validate(draft: RoutineBlockDraft): RoutineCommandResult.Invalid? = when {
        draft.title.isBlank() -> RoutineCommandResult.Invalid("title is required")
        runCatching { LocalTime.parse(draft.startTime) }.isFailure -> RoutineCommandResult.Invalid("startTime must be HH:mm")
        draft.durationMinutes !in RoutineLimits.MIN_DURATION_MINUTES..RoutineLimits.MAX_DURATION_MINUTES ->
            RoutineCommandResult.Invalid("durationMinutes must be ${RoutineLimits.MIN_DURATION_MINUTES}..${RoutineLimits.MAX_DURATION_MINUTES}")
        draft.days.any { it !in 1..7 } -> RoutineCommandResult.Invalid("days must be ISO weekday numbers 1..7")
        draft.date != null && runCatching { LocalDate.parse(draft.date) }.isFailure -> RoutineCommandResult.Invalid("date must be yyyy-MM-dd")
        else -> null
    }
    // ===== END SECTION: ROUTINE_COMMAND_EXECUTOR_BLOCKS =====

    private companion object {
        const val MAX_LIST_DAYS = 31
    }
}
// ===== END SECTION: ROUTINE_COMMAND_EXECUTOR =====

// ===== SECTION: ROUTINE_DRAFT_MAPPING =====
/** The editable form of a stored block. */
fun RoutineBlock.toDraft(): RoutineBlockDraft = RoutineBlockDraft(
    title = title,
    kind = kind,
    subjectId = subject?.id,
    startTime = formatMinute(startMinute),
    durationMinutes = durationMinutes,
    days = Weekdays.toIsoNumbers(daysMask),
    date = date?.toString(),
    notes = notes,
    linkedTaskIds = linkedTasks.map { it.id },
    automation = automation,
)

/** An unsaved block built from a draft, for conflict checks before saving. */
fun RoutineBlockDraft.toCandidate(id: Long, today: LocalDate): RoutineBlock {
    val mask = daysMask
    return RoutineBlock(
        id = id,
        title = title,
        kind = kind,
        subject = null,
        startMinute = startMinute.coerceIn(0, MINUTES_PER_DAY - 1),
        durationMinutes = durationMinutes.coerceIn(RoutineLimits.MIN_DURATION_MINUTES, RoutineLimits.MAX_DURATION_MINUTES),
        daysMask = mask,
        date = if (mask == Weekdays.NONE) (parsedDate ?: today) else null,
        notes = notes,
        linkedTasks = emptyList(),
        automation = automation,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
    )
}

/** "HH:mm" for a minute of the day (wraps past midnight). */
fun formatMinute(minute: Int): String {
    val wrapped = minute.mod(MINUTES_PER_DAY)
    return String.format(Locale.ROOT, "%02d:%02d", wrapped / 60, wrapped % 60)
}

private fun RoutineBlock.toSummary(): RoutineBlockSummary = RoutineBlockSummary(
    id = id,
    title = title,
    kind = kind.name,
    subject = subject?.name,
    start = formatMinute(startMinute),
    end = formatMinute(endMinute),
    days = Weekdays.toIsoNumbers(daysMask),
    date = date?.toString(),
    linkedTaskIds = linkedTasks.map { it.id },
)

private fun RoutineConflict.toSummary(): RoutineConflictSummary = RoutineConflictSummary(
    blockId = block.id,
    title = block.title,
    date = date.toString(),
    start = formatMinute(overlapStartMinute),
    end = formatMinute(overlapEndMinute),
)
// ===== END SECTION: ROUTINE_DRAFT_MAPPING =====
