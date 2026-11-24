package com.kaushalkr.axiom.navigation

import androidx.compose.animation.AnimatedVisibility
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
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
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
    val motion = LocalAxiomMotion.current
    val enter = fadeIn(motion.tween(AxiomMotionTokens.ChromeFadeIn)) +
        scaleIn(motion.spring(AxiomMotionTokens.ChromeShow), initialScale = AxiomMotionTokens.CHROME_HIDDEN_SCALE)
    val exit = fadeOut(motion.tween(AxiomMotionTokens.ChromeFadeOut)) +
        scaleOut(motion.tween(AxiomMotionTokens.ChromeHide), targetScale = AxiomMotionTokens.CHROME_HIDDEN_SCALE)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AxiomDimens.topBarHeight),
    ) {
        AnimatedVisibility(
            visible = !isTopLevel,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = enter,
            exit = exit,
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
            enter = enter,
            exit = exit,
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
