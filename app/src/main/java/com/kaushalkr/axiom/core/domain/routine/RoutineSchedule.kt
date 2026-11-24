package com.kaushalkr.axiom.core.domain.routine

import androidx.compose.runtime.Immutable
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineBlock
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min

// ===== SECTION: ROUTINE_OCCURRENCE =====
/**
 * The part of a block that falls on one [date], in minutes after that midnight. A block that
 * runs past midnight yields two occurrences: its start day ([continuesNextDay]) and the
 * following morning ([isContinuation]).
 */
@Immutable
data class RoutineOccurrence(
    val block: RoutineBlock,
    val date: LocalDate,
    val startMinute: Int,
    val endMinute: Int,
    val isContinuation: Boolean,
    val continuesNextDay: Boolean,
) {
    val durationMinutes: Int get() = endMinute - startMinute

    /** Stable list key: one block can appear twice on a day only as start and continuation. */
    val key: String get() = "${block.id}-${date.toEpochDay()}-${if (isContinuation) "c" else "s"}"

    /**
     * Minutes of the whole block that have passed at [minuteOfDay] on [date], including the
     * part before midnight for a continuation. Clamped to the block's duration.
     */
    fun elapsedMinutes(minuteOfDay: Int): Int {
        val sinceStart = if (isContinuation) minuteOfDay + MINUTES_PER_DAY - block.startMinute else minuteOfDay - block.startMinute
        return sinceStart.coerceIn(0, block.durationMinutes)
    }

    /** Minutes left in the whole block at [minuteOfDay] on [date]. */
    fun remainingMinutes(minuteOfDay: Int): Int = block.durationMinutes - elapsedMinutes(minuteOfDay)
}

/** An occurrence placed in a side-by-side lane, so overlapping blocks never cover each other. */
@Immutable
data class LanedOccurrence(
    val occurrence: RoutineOccurrence,
    val lane: Int,
    val laneCount: Int,
)

/** A gap in the timetable, in minutes after midnight. */
@Immutable
data class FreeSlot(
    val startMinute: Int,
    val endMinute: Int,
) {
    val durationMinutes: Int get() = endMinute - startMinute
}

/** What is happening now and next, for Home and (later) automatic focus sessions. */
@Immutable
data class RoutineNow(
    val ongoing: List<RoutineOccurrence>,
    val upcoming: List<RoutineOccurrence>,
    /** Tomorrow's first block when nothing is left today. */
    val tomorrowFirst: RoutineOccurrence?,
    val todayCount: Int,
)
// ===== END SECTION: ROUTINE_OCCURRENCE =====

// ===== SECTION: ROUTINE_SCHEDULE =====
/** Pure timetable rules, free of Android so they are unit tested and run off the main thread. */
object RoutineSchedule {

    /** Every block visible on [date], ordered by start (longer first on ties). */
    fun occurrencesOn(blocks: List<RoutineBlock>, date: LocalDate): List<RoutineOccurrence> {
        val previous = date.minusDays(1)
        val result = ArrayList<RoutineOccurrence>()
        blocks.forEach { block ->
            if (block.startsOn(date)) {
                result += RoutineOccurrence(
                    block = block,
                    date = date,
                    startMinute = block.startMinute,
                    endMinute = min(block.endMinute, MINUTES_PER_DAY),
                    isContinuation = false,
                    continuesNextDay = block.crossesMidnight,
                )
            }
            if (block.crossesMidnight && block.startsOn(previous)) {
                result += RoutineOccurrence(
                    block = block,
                    date = date,
                    startMinute = 0,
                    endMinute = block.endMinute - MINUTES_PER_DAY,
                    isContinuation = true,
                    continuesNextDay = false,
                )
            }
        }
        result.sortWith(compareBy<RoutineOccurrence> { it.startMinute }.thenByDescending { it.durationMinutes }.thenBy { it.block.id })
        return result
    }

    /**
     * Assigns lanes to overlapping occurrences (greedy interval colouring). Occurrences in one
     * overlapping cluster share its lane count, so they split the width evenly.
     *
     * @param minSpanMinutes treat every occurrence as at least this long, so short blocks drawn
     *   at a minimum height never visually cover the next one.
     */
    fun layoutLanes(occurrences: List<RoutineOccurrence>, minSpanMinutes: Int = 0): List<LanedOccurrence> {
        fun visualEnd(occurrence: RoutineOccurrence): Int = max(occurrence.endMinute, occurrence.startMinute + minSpanMinutes)
        val sorted = occurrences.sortedWith(compareBy<RoutineOccurrence> { it.startMinute }.thenByDescending { it.durationMinutes })
        val result = ArrayList<LanedOccurrence>(sorted.size)
        val cluster = ArrayList<Pair<RoutineOccurrence, Int>>()
        val laneEnds = ArrayList<Int>()
        var clusterEnd = Int.MIN_VALUE

        fun flushCluster() {
            val count = laneEnds.size
            cluster.forEach { (occurrence, lane) -> result += LanedOccurrence(occurrence, lane, count) }
            cluster.clear()
            laneEnds.clear()
            clusterEnd = Int.MIN_VALUE
        }

        sorted.forEach { occurrence ->
            if (cluster.isNotEmpty() && occurrence.startMinute >= clusterEnd) flushCluster()
            val end = visualEnd(occurrence)
            var lane = laneEnds.indexOfFirst { it <= occurrence.startMinute }
            if (lane == -1) {
                lane = laneEnds.size
                laneEnds += end
            } else {
                laneEnds[lane] = end
            }
            cluster += occurrence to lane
            clusterEnd = max(clusterEnd, end)
        }
        flushCluster()
        return result
    }

    /** Ongoing and upcoming blocks at [now]. */
    fun now(blocks: List<RoutineBlock>, now: LocalDateTime, upcomingLimit: Int = 3): RoutineNow {
        val today = now.toLocalDate()
        val minute = now.hour * 60 + now.minute
        val todays = occurrencesOn(blocks, today)
        val ongoing = todays.filter { minute >= it.startMinute && minute < it.endMinute }
        val upcoming = todays.filter { it.startMinute > minute }.take(upcomingLimit)
        val tomorrowFirst = if (ongoing.isEmpty() && upcoming.isEmpty()) {
            occurrencesOn(blocks, today.plusDays(1)).firstOrNull { !it.isContinuation }
        } else {
            null
        }
        return RoutineNow(ongoing = ongoing, upcoming = upcoming, tomorrowFirst = tomorrowFirst, todayCount = todays.size)
    }

    /** Gaps of at least [minDurationMinutes] between [fromMinute] and [toMinute] on [date]. */
    fun freeSlots(
        blocks: List<RoutineBlock>,
        date: LocalDate,
        minDurationMinutes: Int,
        fromMinute: Int = 0,
        toMinute: Int = MINUTES_PER_DAY,
    ): List<FreeSlot> {
        val busy = occurrencesOn(blocks, date).sortedBy { it.startMinute }
        val slots = ArrayList<FreeSlot>()
        var cursor = fromMinute
        busy.forEach { occurrence ->
            if (occurrence.startMinute > cursor) {
                val end = min(occurrence.startMinute, toMinute)
                if (end - cursor >= minDurationMinutes) slots += FreeSlot(cursor, end)
            }
            cursor = max(cursor, occurrence.endMinute)
        }
        if (toMinute - cursor >= minDurationMinutes) slots += FreeSlot(cursor, toMinute)
        return slots
    }

    /** Total planned minutes on [date], counting after-midnight continuations. */
    fun plannedMinutes(blocks: List<RoutineBlock>, date: LocalDate): Int =
        occurrencesOn(blocks, date).sumOf { it.durationMinutes }
}
// ===== END SECTION: ROUTINE_SCHEDULE =====
