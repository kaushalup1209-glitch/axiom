package com.kaushalkr.axiom.core.ui

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomIconBadge
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.AxiomScreenColumn
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.components.ScreenHeader
import com.kaushalkr.axiom.core.designsystem.layout.LocalAxiomWidthClass
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: FEATURE_PLACEHOLDER_SPEC =====
/** Resource bundle describing a feature that is planned for a later phase. */
@Immutable
data class FeaturePlaceholderSpec(
    @param:StringRes val eyebrowRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:StringRes val phaseRes: Int,
    @param:StringRes val heroTitleRes: Int,
    @param:StringRes val heroBodyRes: Int,
    @param:ArrayRes val plannedFeaturesRes: Int,
    @param:DrawableRes val iconRes: Int,
)
// ===== END SECTION: FEATURE_PLACEHOLDER_SPEC =====

// ===== SECTION: FEATURE_PLACEHOLDER_SCREEN =====
/**
 * Shared Phase 01 placeholder layout: header, a hero glass card naming the delivery phase,
 * and a glass card listing the planned capabilities. Contains no feature logic.
 * Uses two columns on medium and expanded widths.
 */
@Composable
fun FeaturePlaceholderScreen(
    spec: FeaturePlaceholderSpec,
    modifier: Modifier = Modifier,
) {
    val isWide = LocalAxiomWidthClass.current.isWide
    AxiomScreenColumn(modifier = modifier) {
        ScreenHeader(
            eyebrow = stringResource(spec.eyebrowRes),
            title = stringResource(spec.titleRes),
            subtitle = stringResource(spec.subtitleRes),
            modifier = Modifier.padding(bottom = AxiomSpacing.xs),
        )
        if (isWide) {
            Row(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.lg)) {
                PlaceholderHeroCard(spec, Modifier.weight(1f))
                PlannedFeaturesCard(spec, Modifier.weight(1f))
            }
        } else {
            PlaceholderHeroCard(spec, Modifier.fillMaxWidth())
            PlannedFeaturesCard(spec, Modifier.fillMaxWidth())
        }
    }
}
// ===== END SECTION: FEATURE_PLACEHOLDER_SCREEN =====

// ===== SECTION: FEATURE_PLACEHOLDER_HERO_CARD =====
@Composable
private fun PlaceholderHeroCard(
    spec: FeaturePlaceholderSpec,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            AxiomIconBadge(iconRes = spec.iconRes)
            AxiomPill(text = stringResource(R.string.placeholder_arriving_in, stringResource(spec.phaseRes)))
        }
        Spacer(Modifier.height(AxiomSpacing.lg))
        Text(
            text = stringResource(spec.heroTitleRes),
            style = AxiomTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(spec.heroBodyRes),
            style = AxiomTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}
// ===== END SECTION: FEATURE_PLACEHOLDER_HERO_CARD =====

// ===== SECTION: FEATURE_PLACEHOLDER_PLANNED_CARD =====
@Composable
private fun PlannedFeaturesCard(
    spec: FeaturePlaceholderSpec,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val features = stringArrayResource(spec.plannedFeaturesRes)
    GlassCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.placeholder_planned_title),
            style = AxiomTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(AxiomSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            features.forEach { feature ->
                PlannedFeatureLine(text = feature)
            }
        }
    }
}

@Composable
private fun PlannedFeatureLine(text: String) {
    val colors = AxiomTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.accent),
        )
        Spacer(Modifier.size(AxiomSpacing.sm))
        Text(
            text = text,
            style = AxiomTheme.typography.bodyMedium,
            color = colors.textPrimary,
        )
    }
}
// ===== END SECTION: FEATURE_PLACEHOLDER_PLANNED_CARD =====
