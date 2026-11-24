package com.kaushalkr.axiom.feature.routine

import androidx.compose.runtime.Immutable
import com.kaushalkr.axiom.core.domain.routine.LanedOccurrence
import com.kaushalkr.axiom.core.domain.routine.RoutineOccurrence
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.core.model.RoutineKind
import java.time.LocalDate
import java.time.YearMonth

// ===== SECTION: ROUTINE_VIEW =====
/** The three timetable views. */
enum class RoutineView {
    DAY,
    WEEK,
    MONTH,
}
// ===== END SECTION: ROUTINE_VIEW =====

// ===== SECTION: ROUTINE_UI_MODELS =====
/** One day of the timeline, with lanes already assigned. */
@Immutable
data class RoutineDayUi(
    val date: LocalDate,
    val occurrences: List<LanedOccurrence>,
    val plannedMinutes: Int,
    val exams: List<Exam>,
)

/** One day card of the week view. */
@Immutable
data class RoutineWeekDayUi(
    val date: LocalDate,
    val occurrences: List<RoutineOccurrence>,
    val plannedMinutes: Int,
    val exams: List<Exam>,
)

/** What a month-grid day shows: which kinds are planned and whether something is due. */
@Immutable
data class RoutineDayMark(
    val kinds: List<RoutineKind>,
    val blockCount: Int,
    val examCount: Int,
)

@Immutable
data class RoutineMonthUi(
    val month: YearMonth,
    val marks: Map<LocalDate, RoutineDayMark>,
    val exams: List<Exam>,
)

@Immutable
data class RoutineUiState(
    val view: RoutineView,
    val selectedDate: LocalDate,
    val today: LocalDate,
    /** Minutes after midnight now; drives the current-time line and the ongoing highlight. */
    val nowMinute: Int,
    val day: RoutineDayUi,
    val week: List<RoutineWeekDayUi>,
    val month: RoutineMonthUi,
    val hasAnyBlocks: Boolean,
) {
    val isOnCurrentPeriod: Boolean
        get() = when (view) {
            RoutineView.DAY -> selectedDate == today
            RoutineView.WEEK -> week.any { it.date == today }
            RoutineView.MONTH -> month.month == YearMonth.from(today)
        }
}
// ===== END SECTION: ROUTINE_UI_MODELS =====
