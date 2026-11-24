package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: AXIOM_PROGRESS_RING =====
/**
 * Circular progress with a soft accent sweep. The fill animates on a critically damped
 * spring and is read only in the draw phase, so animating never recomposes [content].
 *
 * @param progress 0..1; values above 1 draw a full ring.
 */
@Composable
fun AxiomProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val colors = AxiomTheme.colors
    val motion = LocalAxiomMotion.current
    val target = progress.coerceIn(0f, 1f)
    val fill = remember { Animatable(target) }
    LaunchedEffect(target, motion) {
        fill.animateTo(target, motion.spring(AxiomMotionTokens.ProgressFill))
    }

    val trackColor = colors.controlTrack
    val accent = colors.accent
    Box(
        modifier = modifier
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) }
            .drawWithCache {
                val strokePx = strokeWidth.toPx()
                val inset = strokePx / 2f
                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                val topLeft = Offset(inset, inset)
                val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)
                val sweepBrush = Brush.sweepGradient(
                    0f to accent.copy(alpha = 0.55f),
                    1f to accent,
                    center = Offset(size.width / 2f, size.height / 2f),
                )
                onDrawBehind {
                    drawArc(trackColor, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
                    val sweep = 360f * fill.value
                    if (sweep > 0f) {
                        // Start at 12 o'clock; the gradient brightens toward the leading edge.
                        rotate(-90f) {
                            drawArc(sweepBrush, 0f, sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content,
    )
}
// ===== END SECTION: AXIOM_PROGRESS_RING =====

