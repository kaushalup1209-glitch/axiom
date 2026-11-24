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
