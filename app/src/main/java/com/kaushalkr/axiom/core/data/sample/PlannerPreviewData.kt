package com.kaushalkr.axiom.core.data.sample

import androidx.compose.runtime.Immutable
import java.time.LocalTime

// =====================================================================================
// PLACEHOLDER DATA — NOT REAL APPLICATION DATA
// Tasks (Phase 03) and routines (Phase 04) do not exist yet. This file supplies fixed
// preview items so the Home dashboard layout can be reviewed. Every screen that shows
// them labels them "Preview". Delete this file when the Planner repositories land.
// =====================================================================================

// ===== SECTION: SAMPLE_PLANNER_MODELS =====
@Immutable
data class PreviewRoutineBlock(
    val id: Int,
    val start: LocalTime,
    val durationMinutes: Int,
    val title: String,
    val subject: String,
)

@Immutable
data class PreviewTask(
    val id: Int,
    val title: String,
    val subject: String,
    val isDone: Boolean,
)

@Immutable
data class PlannerPreview(
    val routine: List<PreviewRoutineBlock>,
    val tasks: List<PreviewTask>,
) {
    val completedTasks: Int = tasks.count { it.isDone }
}
// ===== END SECTION: SAMPLE_PLANNER_MODELS =====

// ===== SECTION: SAMPLE_PLANNER_DATA =====
/** Fixed sample content. Always the same instance, so it never triggers recomposition. */
object PlannerPreviewData {
    val Default = PlannerPreview(
        routine = listOf(
            PreviewRoutineBlock(1, LocalTime.of(7, 0), 45, "Morning revision", "Physics"),
            PreviewRoutineBlock(2, LocalTime.of(16, 30), 60, "Problem practice", "Mathematics"),
            PreviewRoutineBlock(3, LocalTime.of(19, 0), 50, "Reading block", "History"),
            PreviewRoutineBlock(4, LocalTime.of(21, 30), 20, "Plan tomorrow", "Planning"),
        ),
        tasks = listOf(
            PreviewTask(1, "Finish chapter 4 notes", "Biology", isDone = true),
            PreviewTask(2, "Solve 20 integration problems", "Mathematics", isDone = true),
            PreviewTask(3, "Revise organic reactions", "Chemistry", isDone = false),
            PreviewTask(4, "Draft essay outline", "English", isDone = false),
            PreviewTask(5, "Past paper, section B", "Physics", isDone = false),
        ),
    )
}
// ===== END SECTION: SAMPLE_PLANNER_DATA =====
