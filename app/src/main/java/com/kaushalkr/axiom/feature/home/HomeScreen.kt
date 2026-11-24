package com.kaushalkr.axiom.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderScreen
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderSpec

// ===== SECTION: HOME_PLACEHOLDER_SPEC =====
private val HomePlaceholderSpec = FeaturePlaceholderSpec(
    eyebrowRes = R.string.home_eyebrow,
    titleRes = R.string.home_title,
    subtitleRes = R.string.home_subtitle,
    phaseRes = R.string.home_phase,
    heroTitleRes = R.string.home_hero_title,
    heroBodyRes = R.string.home_hero_body,
    plannedFeaturesRes = R.array.home_planned_features,
    iconRes = R.drawable.ic_nav_home,
)
// ===== END SECTION: HOME_PLACEHOLDER_SPEC =====

// ===== SECTION: HOME_SCREEN =====
/** Home dashboard. Phase 01 placeholder; the dashboard is built in Phase 02. */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(spec = HomePlaceholderSpec, modifier = modifier)
}
// ===== END SECTION: HOME_SCREEN =====
