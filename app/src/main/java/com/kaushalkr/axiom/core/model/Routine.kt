package com.kaushalkr.axiom.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: MODEL_ROUTINE_KIND =====
/** What a timetable block is for. Study blocks drive focus and distraction features later. */
@Serializable
enum class RoutineKind {
    STUDY,
    BREAK,
    SLEEP,
    PERSONAL,
}
// ===== END SECTION: MODEL_ROUTINE_KIND =====

// ===== SECTION: MODEL_WEEKDAYS =====
/**
 * Weekday sets stored as a 7-bit mask: bit 0 is Monday … bit 6 is Sunday (ISO order).
 * A mask of 0 means the block is a one-off on a single date.
 */
object Weekdays {
    const val NONE = 0
    const val EVERY_DAY = 0b111_1111
    const val MONDAY_TO_FRIDAY = 0b001_1111
    const val WEEKEND = 0b110_0000

    fun bit(day: DayOfWeek): Int = 1 shl (day.value - 1)

    fun contains(mask: Int, day: DayOfWeek): Boolean = mask and bit(day) != 0

    fun toggle(mask: Int, day: DayOfWeek): Int = mask xor bit(day)

    fun days(mask: Int): List<DayOfWeek> = DayOfWeek.values().filter { contains(mask, it) }

    fun maskOf(days: Iterable<DayOfWeek>): Int = days.fold(NONE) { mask, day -> mask or bit(day) }

    /** ISO day numbers (1 = Monday … 7 = Sunday), as used in assistant JSON. */
    fun fromIsoNumbers(numbers: Iterable<Int>): Int =
        maskOf(numbers.filter { it in 1..7 }.map(DayOfWeek::of))

    fun toIsoNumbers(mask: Int): List<Int> = days(mask).map { it.value }
}
// ===== END SECTION: MODEL_WEEKDAYS =====

// ===== SECTION: MODEL_ROUTINE_BLOCK =====
/**
 * Settings saved with a block for features that arrive later: an automatic focus timer
 * (Phase 05), distraction tracking (Phases 06–07) and app restrictions (Phase 08).
 * Stored now so existing routines are ready when those features ship.
 */
@Immutable
@Serializable
data class RoutineAutomation(
    val autoStartFocus: Boolean = false,
    val trackDistractions: Boolean = false,
    val restrictApps: Boolean = false,
) {
    val isAnyEnabled: Boolean get() = autoStartFocus || trackDistractions || restrictApps
}

/** A to-do task linked to a block; tasks are linked by id and shown with their live state. */
@Immutable
data class LinkedTask(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
)

/** The subject a block or exam belongs to. */
@Immutable
data class SubjectRef(
    val id: Long,
    val name: String,
    val colorIndex: Int,
)

/**
 * A timetable block. It repeats on [daysMask] weekdays, or happens once on [date] when the
 * mask is empty. A block may run past midnight ([endMinute] > 1440), e.g. sleep.
 */
@Immutable
data class RoutineBlock(
    val id: Long,
    val title: String,
    val kind: RoutineKind,
    val subject: SubjectRef?,
    /** Minutes after midnight, 0..1439. */
    val startMinute: Int,
    val durationMinutes: Int,
    val daysMask: Int,
    val date: LocalDate?,
    val notes: String,
    val linkedTasks: List<LinkedTask>,
    val automation: RoutineAutomation,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    val endMinute: Int get() = startMinute + durationMinutes
    val isRecurring: Boolean get() = date == null
    val crossesMidnight: Boolean get() = endMinute > MINUTES_PER_DAY
    val startTime: LocalTime get() = LocalTime.of(startMinute / 60, startMinute % 60)
    val endTime: LocalTime get() = (endMinute % MINUTES_PER_DAY).let { LocalTime.of(it / 60, it % 60) }

    /** True if the block starts on [day]. */
    fun startsOn(day: LocalDate): Boolean = date?.let { it == day } ?: Weekdays.contains(daysMask, day.dayOfWeek)
}

const val MINUTES_PER_DAY = 24 * 60
// ===== END SECTION: MODEL_ROUTINE_BLOCK =====

// ===== SECTION: MODEL_ROUTINE_DRAFT =====
/**
 * Everything needed to create or replace a block. Serializable so the AI assistant
 * (Phase 11) can propose routines as structured data before anything is saved.
 *
 * @param days ISO weekday numbers (1 = Monday … 7 = Sunday); empty for a one-off [date].
 */
@Immutable
@Serializable
data class RoutineBlockDraft(
    val title: String,
    val kind: RoutineKind = RoutineKind.STUDY,
    val subjectId: Long? = null,
    /** Local time HH:mm. */
    val startTime: String,
    val durationMinutes: Int,
    val days: List<Int> = emptyList(),
    /** ISO-8601 date (yyyy-MM-dd) for a one-off block. */
    val date: String? = null,
    val notes: String = "",
    val linkedTaskIds: List<Long> = emptyList(),
    val automation: RoutineAutomation = RoutineAutomation(),
) {
    val daysMask: Int get() = Weekdays.fromIsoNumbers(days)
    val parsedDate: LocalDate? get() = date?.let(LocalDate::parse)
    val startMinute: Int get() = LocalTime.parse(startTime).let { it.hour * 60 + it.minute }
}

object RoutineLimits {
    const val TITLE_MAX_LENGTH = 80
    const val NOTES_MAX_LENGTH = 1_000
    const val MIN_DURATION_MINUTES = 5
    const val MAX_DURATION_MINUTES = 16 * 60
    const val MAX_LINKED_TASKS = 12

    /** Drag and the time pickers snap to this grid. */
    const val SNAP_MINUTES = 5
    val DURATION_PRESETS_MINUTES = listOf(15, 25, 30, 45, 60, 90, 120, 180, 480)
}
// ===== END SECTION: MODEL_ROUTINE_DRAFT =====
