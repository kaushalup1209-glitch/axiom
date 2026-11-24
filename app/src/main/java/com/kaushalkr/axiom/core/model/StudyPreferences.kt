package com.kaushalkr.axiom.core.model

// ===== SECTION: MODEL_STUDY_DEFAULTS =====
/** Study rules and defaults shared by data, domain and UI. */
object StudyDefaults {
    const val DAILY_GOAL_MINUTES = 240
    const val DAILY_GOAL_MIN_MINUTES = 30
    const val DAILY_GOAL_MAX_MINUTES = 600
    const val DAILY_GOAL_STEP_MINUTES = 15

    const val QUICK_START_MINUTES = 25
    val QUICK_START_PRESETS_MINUTES = listOf(15, 25, 45, 60)

    /** A day counts toward the streak once this much focused time is recorded. */
    const val STREAK_MINIMUM_MINUTES = 15

    /** Sessions shorter than this are discarded instead of saved. */
    const val MINIMUM_RECORDED_SESSION_MILLIS = 60_000L

    const val USER_NAME_MAX_LENGTH = 24
}
// ===== END SECTION: MODEL_STUDY_DEFAULTS =====

// ===== SECTION: MODEL_STUDY_PREFERENCES =====
/** Persisted personal and study preferences. */
data class StudyPreferences(
    /** Name used in the greeting; blank means a greeting without a name. */
    val userName: String = "",
    val dailyGoalMinutes: Int = StudyDefaults.DAILY_GOAL_MINUTES,
    /** Last quick-start focus length, reused as the next default. */
    val quickStartMinutes: Int = StudyDefaults.QUICK_START_MINUTES,
)
// ===== END SECTION: MODEL_STUDY_PREFERENCES =====
