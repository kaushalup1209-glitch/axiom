package com.kaushalkr.axiom.feature.planner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderScreen
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderSpec

// ===== SECTION: PLANNER_PLACEHOLDER_SPEC =====
private val PlannerPlaceholderSpec = FeaturePlaceholderSpec(
    eyebrowRes = R.string.planner_eyebrow,
    titleRes = R.string.planner_title,
    subtitleRes = R.string.planner_subtitle,
    phaseRes = R.string.planner_phase,
    heroTitleRes = R.string.planner_hero_title,
    heroBodyRes = R.string.planner_hero_body,
    plannedFeaturesRes = R.array.planner_planned_features,
    iconRes = R.drawable.ic_nav_planner,
)
// ===== END SECTION: PLANNER_PLACEHOLDER_SPEC =====

// ===== SECTION: PLANNER_SCREEN =====
/** Planner. Phase 01 placeholder; tasks arrive in Phase 03 and routines in Phase 04. */
@Composable
fun PlannerScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(spec = PlannerPlaceholderSpec, modifier = modifier)
}
// ===== END SECTION: PLANNER_SCREEN =====
