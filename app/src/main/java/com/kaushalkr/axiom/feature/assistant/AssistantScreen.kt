package com.kaushalkr.axiom.feature.assistant

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderScreen
import com.kaushalkr.axiom.core.ui.FeaturePlaceholderSpec

// ===== SECTION: ASSISTANT_PLACEHOLDER_SPEC =====
private val AssistantPlaceholderSpec = FeaturePlaceholderSpec(
    eyebrowRes = R.string.assistant_eyebrow,
    titleRes = R.string.assistant_title,
    subtitleRes = R.string.assistant_subtitle,
    phaseRes = R.string.assistant_phase,
    heroTitleRes = R.string.assistant_hero_title,
    heroBodyRes = R.string.assistant_hero_body,
    plannedFeaturesRes = R.array.assistant_planned_features,
    iconRes = R.drawable.ic_nav_assistant,
)
// ===== END SECTION: ASSISTANT_PLACEHOLDER_SPEC =====

// ===== SECTION: ASSISTANT_SCREEN =====
/** AI Discipline Assistant. Phase 01 placeholder; the assistant arrives in Phases 11–13. */
@Composable
fun AssistantScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(spec = AssistantPlaceholderSpec, modifier = modifier)
}
// ===== END SECTION: ASSISTANT_SCREEN =====
