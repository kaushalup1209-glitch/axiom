package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: AXIOM_LINEAR_PROGRESS =====
/**
 * Capsule progress bar. The fill animates in the draw phase only. Pass [animate] = false for
 * values that tick (timers) so each step draws a single frame.
 */
@Composable
fun AxiomLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    animate: Boolean = true,
) {
    val colors = AxiomTheme.colors
    val motion = LocalAxiomMotion.current
    val target = progress.coerceIn(0f, 1f)
    val fill = remember { Animatable(target) }
    LaunchedEffect(target, motion, animate) {
        if (animate) fill.animateTo(target, motion.spring(AxiomMotionTokens.ProgressFill)) else fill.snapTo(target)
    }
    val trackColor = colors.controlTrack
    val accent = colors.accent
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) }
            .drawBehind {
                val radius = CornerRadius(size.height / 2f)
                drawRoundRect(trackColor, cornerRadius = radius)
                val width = size.width * fill.value
                if (width > 0f) {
                    drawRoundRect(accent, size = Size(maxOf(width, size.height), size.height), cornerRadius = radius)
                }
            },
    )
}
// ===== END SECTION: AXIOM_LINEAR_PROGRESS =====
