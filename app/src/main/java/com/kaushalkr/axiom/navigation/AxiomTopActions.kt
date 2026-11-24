package com.kaushalkr.axiom.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassIconButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing

// ===== SECTION: NAV_FLOATING_TOP_ACTIONS =====
/**
 * Floating glass buttons above the content. Primary tabs show Distraction Shield and
 * Settings at the trailing edge; secondary screens show a Back button at the leading edge.
 */
@Composable
fun AxiomTopActions(
    isTopLevel: Boolean,
    onOpenDistractionShield: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AxiomDimens.topBarHeight),
    ) {
        AnimatedVisibility(
            visible = !isTopLevel,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.8f),
        ) {
            GlassIconButton(
                onClick = onNavigateBack,
                iconRes = R.drawable.ic_arrow_back,
                contentDescription = stringResource(R.string.action_navigate_back),
            )
        }
        AnimatedVisibility(
            visible = isTopLevel,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.8f),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                GlassIconButton(
                    onClick = onOpenDistractionShield,
                    iconRes = R.drawable.ic_shield,
                    contentDescription = stringResource(R.string.action_open_shield),
                )
                GlassIconButton(
                    onClick = onOpenSettings,
                    iconRes = R.drawable.ic_settings,
                    contentDescription = stringResource(R.string.action_open_settings),
                )
            }
        }
    }
}
// ===== END SECTION: NAV_FLOATING_TOP_ACTIONS =====
