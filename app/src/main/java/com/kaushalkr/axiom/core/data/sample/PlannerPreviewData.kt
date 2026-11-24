package com.kaushalkr.axiom.core.data.sample

import androidx.compose.runtime.Immutable
import java.time.LocalTime

// =====================================================================================
// PLACEHOLDER DATA — NOT REAL APPLICATION DATA
// Routines (Phase 04) do not exist yet. This file supplies a fixed sample routine so the
// Home dashboard layout can be reviewed; the card that shows it is labelled "Preview".
// Tasks are real data since Phase 03 and no longer come from here.
// Delete this file when the routine repository lands.
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
data class PlannerPreview(
    val routine: List<PreviewRoutineBlock>,
)
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
    )
}
// ===== END SECTION: SAMPLE_PLANNER_DATA =====
