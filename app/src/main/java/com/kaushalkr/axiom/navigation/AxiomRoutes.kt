package com.kaushalkr.axiom.navigation

import kotlinx.serialization.Serializable

// ===== SECTION: NAV_TOP_LEVEL_ROUTES =====
/** Type-safe Navigation Compose routes for the five primary tabs. */
@Serializable
data object HomeRoute

@Serializable
data object PlannerRoute

@Serializable
data object FocusRoute

@Serializable
data object InsightsRoute

@Serializable
data object AssistantRoute
// ===== END SECTION: NAV_TOP_LEVEL_ROUTES =====

// ===== SECTION: NAV_SECONDARY_ROUTES =====
/** Secondary destinations reached from the floating top actions. */
@Serializable
data object DistractionShieldRoute

@Serializable
data object SettingsRoute

/**
 * Task editor. [taskId] 0 creates a new task; [dueToday] pre-fills today's date for a new one.
 */
@Serializable
data class TaskEditorRoute(
    val taskId: Long = 0L,
    val dueToday: Boolean = false,
)
// ===== END SECTION: NAV_SECONDARY_ROUTES =====

// ===== SECTION: NAV_PLANNER_ROUTES =====
/**
 * Timetable block editor. [blockId] 0 creates a block; [duplicateOf] pre-fills a copy of
 * another block. [epochDay] ([NO_EPOCH_DAY] for none) and [startMinute] (-1 for none)
 * pre-fill a new block from the timetable slot the user tapped.
 */
@Serializable
data class RoutineBlockRoute(
    val blockId: Long = 0L,
    val duplicateOf: Long = 0L,
    val epochDay: Long = NO_EPOCH_DAY,
    val startMinute: Int = -1,
) {
    companion object {
        const val NO_EPOCH_DAY: Long = Long.MIN_VALUE
    }
}

/** Subject detail with its syllabus chapters and exams. */
@Serializable
data class SubjectRoute(val subjectId: Long)

/** Exam or deadline editor. [examId] 0 creates one; [subjectId] pre-selects its subject. */
@Serializable
data class ExamRoute(
    val examId: Long = 0L,
    val subjectId: Long = 0L,
)
// ===== END SECTION: NAV_PLANNER_ROUTES =====
