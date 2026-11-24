package com.kaushalkr.axiom.core.domain.routine

import androidx.compose.runtime.Immutable
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineBlock
import com.kaushalkr.axiom.core.model.RoutineLimits
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

// ===== SECTION: ROUTINE_CONFLICT =====
/** Another block that overlaps a candidate, with the first date it does so. */
@Immutable
data class RoutineConflict(
    val block: RoutineBlock,
    val date: LocalDate,
    val overlapStartMinute: Int,
    val overlapEndMinute: Int,
)

/** Ways to resolve a conflict, offered before saving. Null entries are not possible. */
@Immutable
data class ConflictResolutions(
    /** The first later start (same length, same days) with no overlap. */
    val nextFreeStartMinute: Int?,
    /** The longest shorter duration (same start) with no overlap. */
    val shortenedDurationMinutes: Int?,
)
// ===== END SECTION: ROUTINE_CONFLICT =====

// ===== SECTION: ROUTINE_CONFLICT_DETECTOR =====
/**
 * Finds overlaps between a candidate block and the rest of the timetable, including blocks that
 * run past midnight, and proposes fixes. Recurring blocks are compared over the coming week (every
 * weekday once) plus the dates of upcoming one-off blocks; one-off blocks on their own date.
 */
object RoutineConflictDetector {

    fun conflicts(candidate: RoutineBlock, others: List<RoutineBlock>, today: LocalDate): List<RoutineConflict> {
        // A new block has id 0, which no saved block has; an edited block skips its saved self.
        val rest = others.filter { it.id != candidate.id }
        val found = LinkedHashMap<Long, RoutineConflict>()
        relevantDates(candidate, rest, today).forEach { date ->
            val mine = RoutineSchedule.occurrencesOn(listOf(candidate), date)
            if (mine.isEmpty()) return@forEach
            val theirs = RoutineSchedule.occurrencesOn(rest, date)
            mine.forEach { a ->
                theirs.forEach { b ->
                    val start = max(a.startMinute, b.startMinute)
                    val end = min(a.endMinute, b.endMinute)
                    if (start < end && b.block.id !in found) {
                        found[b.block.id] = RoutineConflict(b.block, date, start, end)
                    }
                }
            }
        }
        return found.values.toList()
    }

    fun resolutions(candidate: RoutineBlock, others: List<RoutineBlock>, today: LocalDate): ConflictResolutions =
        ConflictResolutions(
            nextFreeStartMinute = nextFreeStart(candidate, others, today),
            shortenedDurationMinutes = shortenedDuration(candidate, others, today),
        )

    /** The first start after the candidate's, on the snap grid, that leaves the block conflict-free. */
    fun nextFreeStart(candidate: RoutineBlock, others: List<RoutineBlock>, today: LocalDate): Int? {
        var start = candidate.startMinute + RoutineLimits.SNAP_MINUTES
        while (start < MINUTES_PER_DAY) {
            if (conflicts(candidate.copy(startMinute = start), others, today).isEmpty()) return start
            start += RoutineLimits.SNAP_MINUTES
        }
        return null
    }

    /** The longest duration below the candidate's that ends before the first overlap. */
    fun shortenedDuration(candidate: RoutineBlock, others: List<RoutineBlock>, today: LocalDate): Int? {
        var duration = candidate.durationMinutes - RoutineLimits.SNAP_MINUTES
        while (duration >= RoutineLimits.MIN_DURATION_MINUTES) {
            if (conflicts(candidate.copy(durationMinutes = duration), others, today).isEmpty()) return duration
            duration -= RoutineLimits.SNAP_MINUTES
        }
        return null
    }

    private fun relevantDates(candidate: RoutineBlock, others: List<RoutineBlock>, today: LocalDate): Set<LocalDate> {
        val dates = sortedSetOf<LocalDate>()
        val oneOff = candidate.date
        if (oneOff != null) {
            dates += oneOff
            if (candidate.crossesMidnight) dates += oneOff.plusDays(1)
        } else {
            // Eight days: every weekday once, plus the morning after the last for overnight blocks.
            (0L..WEEK_DAYS).forEach { dates += today.plusDays(it) }
            others.mapNotNull { it.date }.filter { !it.isBefore(today) }.forEach { date ->
                dates += date
                dates += date.plusDays(1)
            }
        }
        return dates
    }

    private const val WEEK_DAYS = 7L
}
// ===== END SECTION: ROUTINE_CONFLICT_DETECTOR =====
