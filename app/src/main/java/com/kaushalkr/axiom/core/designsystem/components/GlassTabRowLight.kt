package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kaushalkr.axiom.core.designsystem.glass.GlassIndicatorTokens
import com.kaushalkr.axiom.core.designsystem.glass.GlassLiteTokens
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.GlassSurfaceSpec
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.glassSurface
import com.kaushalkr.axiom.core.designsystem.glass.glassSurfaceLayer
import com.kaushalkr.axiom.core.designsystem.glass.interaction.DampedDragAnimation
import com.kaushalkr.axiom.core.designsystem.glass.interaction.InteractiveHighlight
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: GLASS_TAB_ROW_LIGHT_LAYERS =====
/**
 * Lite and Off rendering of [GlassTabRow]: the same drag physics, press growth and accent
 * labels, without capturing the tabs or refracting them.
 *
 * - The container is [glassSurface] (Lite: lightweight glass, or blurred when the row is
 *   prominent, such as the navigation bar) or a solid capsule (Off).
 * - The indicator is a plain capsule drawn in the draw phase from the drag animation, so
 *   dragging never recomposes. While pressed it lifts into a frosted pill.
 * - Labels are drawn twice: normal labels with the indicator area cut out, and accent labels
 *   clipped to the indicator, so the selected tab is accent-coloured exactly as in Full.
 */
@Composable
internal fun LightTabRowLayers(
    style: GlassStyle,
    spec: GlassSurfaceSpec,
    floating: Boolean,
    containerHeight: Dp,
    indicatorHeight: Dp,
    containerPadding: Dp,
    containerColor: Color,
    indicatorColor: Color,
    tabsCount: Int,
    tabWidth: Float,
    isLtr: Boolean,
    panelOffset: () -> Float,
    dragAnimation: DampedDragAnimation,
    interactiveHighlight: InteractiveHighlight?,
    content: @Composable RowScope.() -> Unit,
) {
    val highlightModifier = interactiveHighlight?.modifier ?: Modifier
    val highlightGestureModifier = interactiveHighlight?.gestureModifier ?: Modifier
    val colors = AxiomTheme.colors
    val backdrop = LocalGlassBackdrop.current
    val accentColor = colors.accent
    val pressedFill = if (style.isEnabled) style.chromeSurfaceColor else style.solidSurfaceColor
    val rimColor = when {
        !style.isEnabled -> Color.Transparent
        colors.isDark -> Color.White.copy(alpha = GlassLiteTokens.INDICATOR_RIM_ALPHA_DARK)
        else -> Color.White.copy(alpha = GlassLiteTokens.INDICATOR_RIM_ALPHA_LIGHT)
    }

    // ===== SECTION: GLASS_TAB_ROW_LIGHT_CONTAINER =====
    val containerScale: GraphicsLayerScope.() -> Unit = remember(dragAnimation) {
        {
            val progress = dragAnimation.pressProgress
            val scale = lerp(1f, 1f + GlassIndicatorTokens.PressedContainerGrowth.toPx() / size.width, progress)
            scaleX = scale
            scaleY = scale
        }
    }
    val containerSurface = remember(style, backdrop, spec, containerColor, containerScale, floating) {
        if (style.isEnabled) {
            Modifier.glassSurface(
                backdrop = backdrop,
                style = style,
                spec = spec,
                shape = GlassShapes.Capsule,
                surfaceColor = containerColor,
                layerBlock = containerScale,
            )
        } else {
            Modifier
                .glassSurfaceLayer(GlassShapes.Capsule, if (floating) spec.fallbackElevation else 0.dp, containerScale)
                .background(containerColor)
        }
    }
    Row(
        Modifier
            .graphicsLayer { translationX = panelOffset() }
            .then(containerSurface)
            .then(highlightModifier)
            .height(containerHeight)
            .fillMaxWidth()
            .padding(containerPadding)
            // The accent copy covers the indicator area; cut it out here so labels never double up.
            .drawWithContent {
                val bounds = indicatorBounds(dragAnimation, tabWidth, isLtr)
                clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom, ClipOp.Difference) {
                    this@drawWithContent.drawContent()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
    // ===== END SECTION: GLASS_TAB_ROW_LIGHT_CONTAINER =====

    // ===== SECTION: GLASS_TAB_ROW_LIGHT_INDICATOR =====
    CompositionLocalProvider(
        LocalGlassTabScale provides {
            lerp(1f, GlassIndicatorTokens.PRESSED_CONTENT_SCALE, dragAnimation.pressProgress)
        },
    ) {
        Row(
            Modifier
                .clearAndSetSemantics {}
                .graphicsLayer { translationX = panelOffset() }
                .height(indicatorHeight)
                .fillMaxWidth()
                .padding(horizontal = containerPadding)
                .drawWithContent {
                    val bounds = indicatorBounds(dragAnimation, tabWidth, isLtr)
                    val progress = dragAnimation.pressProgress
                    val radius = CornerRadius(bounds.height / 2f)
                    drawRoundRect(
                        color = lerp(indicatorColor, pressedFill, progress),
                        topLeft = bounds.topLeft,
                        size = bounds.size,
                        cornerRadius = radius,
                    )
                    if (rimColor.alpha > 0f) {
                        drawRoundRect(
                            color = rimColor,
                            topLeft = bounds.topLeft,
                            size = bounds.size,
                            cornerRadius = radius,
                            style = Stroke(width = GlassLiteTokens.RimWidth.toPx()),
                            alpha = 0.35f + 0.65f * progress,
                        )
                    }
                    clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom) {
                        this@drawWithContent.drawContent()
                    }
                }
                .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }

    // Transparent drag handle over the selected tab: the same gesture target as Full.
    Box(
        Modifier
            .padding(horizontal = containerPadding)
            .graphicsLayer {
                translationX =
                    if (isLtr) dragAnimation.value * tabWidth + panelOffset()
                    else size.width - (dragAnimation.value + 1f) * tabWidth + panelOffset()
            }
            .then(highlightGestureModifier)
            .then(dragAnimation.modifier)
            .height(indicatorHeight)
            .fillMaxWidth(1f / tabsCount),
    )
    // ===== END SECTION: GLASS_TAB_ROW_LIGHT_INDICATOR =====
}
// ===== END SECTION: GLASS_TAB_ROW_LIGHT_LAYERS =====

// ===== SECTION: GLASS_TAB_ROW_LIGHT_GEOMETRY =====
/**
 * The indicator in the content coordinates of a tab row, including the pressed growth and
 * the velocity squash used by the Full indicator.
 */
private fun DrawScope.indicatorBounds(drag: DampedDragAnimation, tabWidth: Float, isLtr: Boolean): Rect {
    val value = drag.value
    val left = if (isLtr) value * tabWidth else size.width - (value + 1f) * tabWidth
    val velocity = drag.velocity / VELOCITY_DIVISOR
    val scaleX = drag.scaleX / (1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f))
    val scaleY = drag.scaleY * (1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f))
    val width = tabWidth * scaleX
    val height = size.height * scaleY
    val center = Offset(left + tabWidth / 2f, size.height / 2f)
    return Rect(Offset(center.x - width / 2f, center.y - height / 2f), Size(width, height))
}

private const val VELOCITY_DIVISOR = 10f
// ===== END SECTION: GLASS_TAB_ROW_LIGHT_GEOMETRY =====
