package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import com.kaushalkr.axiom.core.designsystem.glass.GlassLiteTokens
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.interaction.DampedDragAnimation

// ===== SECTION: LIGHT_CONTROL_THUMB =====
/**
 * Toggle and slider thumb for Lite and Off glass. Position, press growth and the velocity
 * squash come from the same [DampedDragAnimation] as the Full lens thumb and are applied in
 * one graphics layer, which also casts a hardware shadow; the body is plain drawing. Nothing
 * here samples a backdrop, so the track is never captured.
 *
 * @param translationX horizontal position, evaluated in the layer (draw phase only).
 * @param velocityDivisor matches the Full thumb's squash response for this control.
 */
@Composable
internal fun LightControlThumb(
    dragAnimation: DampedDragAnimation,
    style: GlassStyle,
    thumbColor: Color,
    velocityDivisor: Float,
    translationX: GraphicsLayerScope.() -> Float,
) {
    val rim = if (style.isEnabled) style.liteRimColors.last() else Color.Transparent
    Box(
        Modifier
            .graphicsLayer {
                this.translationX = translationX()
                val velocity = dragAnimation.velocity / velocityDivisor
                scaleX = dragAnimation.scaleX / (1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f))
                scaleY = dragAnimation.scaleY * (1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f))
                shadowElevation = GlassLiteTokens.ThumbElevation.toPx()
                shape = GlassShapes.Capsule
            }
            .drawBehind {
                // Lite frosts the pressed thumb slightly; Off keeps it opaque.
                val alpha = if (style.isEnabled) 1f - GlassLiteTokens.THUMB_PRESSED_FROST * dragAnimation.pressProgress else 1f
                val radius = CornerRadius(size.height / 2f)
                drawRoundRect(thumbColor.copy(alpha = alpha), cornerRadius = radius)
                if (rim.alpha > 0f) {
                    drawRoundRect(rim, cornerRadius = radius, style = Stroke(width = 1.dp.toPx()))
                }
            }
            .size(40f.dp, 24f.dp),
    )
}
// ===== END SECTION: LIGHT_CONTROL_THUMB =====
