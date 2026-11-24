package com.kaushalkr.axiom.feature.home

import androidx.compose.runtime.Immutable
import com.kaushalkr.axiom.core.data.sample.PlannerPreview
import com.kaushalkr.axiom.core.domain.StudyStreak
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: HOME_DAY_PART =====
enum class DayPart {
    MORNING,
    AFTERNOON,
    EVENING,
    LATE_NIGHT,
    ;

    companion object {
        fun from(time: LocalTime): DayPart = when (time.hour) {
            in 5..11 -> MORNING
            in 12..16 -> AFTERNOON
            in 17..21 -> EVENING
            else -> LATE_NIGHT
        }
    }
}
// ===== END SECTION: HOME_DAY_PART =====

// ===== SECTION: HOME_DASHBOARD_UI_STATE =====
/**
 * Everything on the dashboard except the ticking focus timer. Changes at most once a minute,
 * so the dashboard does not recompose every second while a session runs.
 */
@Immutable
data class HomeDashboardUiState(
    val today: LocalDate,
    /** Current time at minute precision, for "up next" in the routine. */
    val currentTime: LocalTime,
    val dayPart: DayPart,
    val userName: String,
    /** Real data: focused minutes today, including a running session. */
    val studiedTodayMinutes: Int,
    val dailyGoalMinutes: Int,
    /** Real data: streak from recorded focus sessions. */
    val streak: StudyStreak,
    /** Real data: null until Distraction Shield tracking exists (Phase 06–07). */
    val distractionTodayMinutes: Int?,
    /** PLACEHOLDER data: routine and tasks until the Planner exists (Phases 03–04). */
    val plannerPreview: PlannerPreview,
) {
    val goalProgress: Float
        get() = if (dailyGoalMinutes <= 0) 0f else studiedTodayMinutes.toFloat() / dailyGoalMinutes

    val isGoalReached: Boolean get() = studiedTodayMinutes >= dailyGoalMinutes
}
// ===== END SECTION: HOME_DASHBOARD_UI_STATE =====

// ===== SECTION: HOME_QUICK_FOCUS_UI_STATE =====
/** Which controls the quick-focus block shows. Changes only on start, pause, resume or finish. */
@Immutable
sealed interface QuickFocusMode {
    @Immutable
    data class Idle(val selectedMinutes: Int, val presetsMinutes: List<Int>) : QuickFocusMode

    @Immutable
    data class Running(val plannedMinutes: Int) : QuickFocusMode

    @Immutable
    data class Paused(val plannedMinutes: Int) : QuickFocusMode
}

/** The ticking part of the quick-focus block. Read only by the readout composables. */
@Immutable
data class QuickFocusClock(
    val remainingMillis: Long,
    val progress: Float,
)
// ===== END SECTION: HOME_QUICK_FOCUS_UI_STATE =====
