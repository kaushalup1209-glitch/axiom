package com.kaushalkr.axiom.feature.focus

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderScreen
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderSpec

// ===== SECTION: FOCUS_PLACEHOLDER_SPEC =====
private val FocusPlaceholderSpec = FeaturePlaceholderSpec(
    eyebrowRes = R.string.focus_eyebrow,
    titleRes = R.string.focus_title,
    subtitleRes = R.string.focus_subtitle,
    phaseRes = R.string.focus_phase,
    heroTitleRes = R.string.focus_hero_title,
    heroBodyRes = R.string.focus_hero_body,
    plannedFeaturesRes = R.array.focus_planned_features,
    iconRes = R.drawable.ic_nav_focus,
)
// ===== END SECTION: FOCUS_PLACEHOLDER_SPEC =====

// ===== SECTION: FOCUS_SCREEN =====
/** Focus timers. Phase 01 placeholder; the timer system is built in Phase 05. */
@Composable
fun FocusScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(spec = FocusPlaceholderSpec, modifier = modifier)
}
// ===== END SECTION: FOCUS_SCREEN =====
