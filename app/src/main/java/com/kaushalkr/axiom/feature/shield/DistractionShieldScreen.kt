package com.kaushalkr.axiom.feature.shield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderScreen
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderSpec

// ===== SECTION: SHIELD_PLACEHOLDER_SPEC =====
private val DistractionShieldPlaceholderSpec = FeaturePlaceholderSpec(
    eyebrowRes = R.string.shield_eyebrow,
    titleRes = R.string.shield_title,
    subtitleRes = R.string.shield_subtitle,
    phaseRes = R.string.shield_phase,
    heroTitleRes = R.string.shield_hero_title,
    heroBodyRes = R.string.shield_hero_body,
    plannedFeaturesRes = R.array.shield_planned_features,
    iconRes = R.drawable.ic_shield,
)
// ===== END SECTION: SHIELD_PLACEHOLDER_SPEC =====

// ===== SECTION: SHIELD_SCREEN =====
/** Distraction Shield. Phase 01 placeholder; tracking and blocking arrive in Phases 06–08. */
@Composable
fun DistractionShieldScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(spec = DistractionShieldPlaceholderSpec, modifier = modifier)
}
// ===== END SECTION: SHIELD_SCREEN =====
