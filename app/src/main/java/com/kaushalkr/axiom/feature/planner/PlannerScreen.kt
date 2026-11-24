package com.kaushalkr.axiom.feature.planner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.feature.tasks.TaskListRoute

// ===== SECTION: PLANNER_SCREEN =====
/** Planner tab. Phase 03 shows the task list; routines join it in Phase 04. */
@Composable
fun PlannerScreen(
    onOpenTask: (taskId: Long) -> Unit,
    onNewTask: (dueToday: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TaskListRoute(onOpenTask = onOpenTask, onNewTask = onNewTask, modifier = modifier)
}
// ===== END SECTION: PLANNER_SCREEN =====
