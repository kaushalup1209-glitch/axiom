package com.kaushalkr.axiom.feature.home.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomLinearProgress
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.components.GlassSegmentedControl
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.home.QuickFocusClock
import com.kaushalkr.axiom.feature.home.QuickFocusMode
import com.kaushalkr.axiom.feature.home.formatCountdown
import com.kaushalkr.axiom.feature.home.formatStudyMinutes

// ===== SECTION: HOME_QUICK_FOCUS_CARD =====
/**
 * Quick-start focus timer. [mode] changes only on start, pause, resume or finish; the
 * once-a-second [clock] is read only by the readout and progress bar, so the glass card and
 * its controls do not recompose while the timer runs.
 */
@Composable
fun QuickFocusCard(
    mode: QuickFocusMode,
    clock: State<QuickFocusClock>,
    onPresetSelected: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = when (mode) {
        is QuickFocusMode.Idle -> stringResource(R.string.home_focus_status_idle)
        is QuickFocusMode.Running -> stringResource(R.string.home_focus_status_running, formatStudyMinutes(mode.plannedMinutes))
        is QuickFocusMode.Paused -> stringResource(R.string.home_focus_status_paused, formatStudyMinutes(mode.plannedMinutes))
    }
    GlassCard(modifier = modifier, hostsGlass = true) {
        HomeCardHeader(
            iconRes = R.drawable.ic_nav_focus,
            title = stringResource(R.string.home_focus_title),
            subtitle = status,
        )
        Spacer(Modifier.height(AxiomSpacing.lg))
        FocusCountdownReadout(clock)
        Spacer(Modifier.height(AxiomSpacing.sm))
        FocusClockProgress(clock)
        Spacer(Modifier.height(AxiomSpacing.lg))
        QuickFocusControls(mode, onPresetSelected, onStart, onPause, onResume, onFinish)
    }
}
// ===== END SECTION: HOME_QUICK_FOCUS_CARD =====

// ===== SECTION: HOME_QUICK_FOCUS_READOUT =====
@Composable
private fun FocusCountdownReadout(clock: State<QuickFocusClock>) {
    val text = formatCountdown(clock.value.remainingMillis)
    val description = stringResource(R.string.home_focus_remaining_description, text)
    Text(
        text = text,
        style = AxiomTheme.typography.displayMedium,
        color = AxiomTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
    )
}

@Composable
private fun FocusClockProgress(clock: State<QuickFocusClock>) {
    // Steps once a second without animating, so a running timer draws one frame per second.
    AxiomLinearProgress(progress = clock.value.progress, height = 6.dp, animate = false)
}
// ===== END SECTION: HOME_QUICK_FOCUS_READOUT =====

// ===== SECTION: HOME_QUICK_FOCUS_CONTROLS =====
@Composable
private fun QuickFocusControls(
    mode: QuickFocusMode,
    onPresetSelected: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
) {
    val motion = LocalAxiomMotion.current
    AnimatedContent(
        targetState = mode,
        contentKey = { it::class },
        transitionSpec = {
            (fadeIn(motion.tween(AxiomMotionTokens.ContentSwapIn)) togetherWith fadeOut(motion.tween(AxiomMotionTokens.ContentSwapOut)))
                .using(SizeTransform { _, _ -> motion.spring(AxiomMotionTokens.ContentSwapSize, IntSize(1, 1)) })
        },
        label = "QuickFocusControls",
    ) { target ->
        when (target) {
            is QuickFocusMode.Idle -> Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.md)) {
                GlassSegmentedControl(
                    options = target.presetsMinutes.map { stringResource(R.string.home_focus_preset, it) },
                    selectedIndex = target.presetsMinutes.indexOf(target.selectedMinutes).coerceAtLeast(0),
                    onSelect = { index -> onPresetSelected(target.presetsMinutes[index]) },
                    modifier = Modifier.fillMaxWidth(),
                )
                FocusActionButton(
                    iconRes = R.drawable.ic_play,
                    label = stringResource(R.string.home_focus_start),
                    onClick = onStart,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is QuickFocusMode.Running -> Row(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
                FocusActionButton(R.drawable.ic_pause, stringResource(R.string.home_focus_pause), onPause, Modifier.weight(1f))
                FocusActionButton(R.drawable.ic_stop, stringResource(R.string.home_focus_finish), onFinish, Modifier.weight(1f))
            }
            is QuickFocusMode.Paused -> Row(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
                FocusActionButton(
                    iconRes = R.drawable.ic_play,
                    label = stringResource(R.string.home_focus_resume),
                    onClick = onResume,
                    isPrimary = true,
                    modifier = Modifier.weight(1f),
                )
                FocusActionButton(R.drawable.ic_stop, stringResource(R.string.home_focus_finish), onFinish, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FocusActionButton(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
) {
    val colors = AxiomTheme.colors
    GlassButton(
        onClick = onClick,
        modifier = modifier,
        tint = if (isPrimary) colors.accent else Color.Unspecified,
        minHeight = 52.dp,
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(20.dp))
        Text(text = label)
    }
}
// ===== END SECTION: HOME_QUICK_FOCUS_CONTROLS =====
