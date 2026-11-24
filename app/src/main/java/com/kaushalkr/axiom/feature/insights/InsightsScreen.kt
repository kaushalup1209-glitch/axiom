package com.kaushalkr.axiom.feature.insights

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderScreen
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderSpec

// ===== SECTION: INSIGHTS_PLACEHOLDER_SPEC =====
private val InsightsPlaceholderSpec = FeaturePlaceholderSpec(
    eyebrowRes = R.string.insights_eyebrow,
    titleRes = R.string.insights_title,
    subtitleRes = R.string.insights_subtitle,
    phaseRes = R.string.insights_phase,
    heroTitleRes = R.string.insights_hero_title,
    heroBodyRes = R.string.insights_hero_body,
    plannedFeaturesRes = R.array.insights_planned_features,
    iconRes = R.drawable.ic_nav_insights,
)
// ===== END SECTION: INSIGHTS_PLACEHOLDER_SPEC =====

// ===== SECTION: INSIGHTS_SCREEN =====
/** Productivity insights. Phase 01 placeholder; analytics arrive in Phases 09–10. */
@Composable
fun InsightsScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(spec = InsightsPlaceholderSpec, modifier = modifier)
}
// ===== END SECTION: INSIGHTS_SCREEN =====
