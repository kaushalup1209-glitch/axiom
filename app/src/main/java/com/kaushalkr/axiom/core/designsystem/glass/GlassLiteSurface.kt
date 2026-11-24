package com.kaushalkr.axiom.core.designsystem.glass

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ===== SECTION: GLASS_LITE_SURFACE =====
/**
 * Lightweight glass for Lite's standard surfaces (cards, task rows, timer blocks, buttons,
 * inputs): a translucent vertical gradient, the surface's sheen band and a thin rim that
 * catches light at the top. One graphics layer carries the clip, the optional press
 * deformation ([layerBlock]) and an optional hardware elevation shadow; everything else is
 * plain drawing cached per size. No backdrop, blur, RenderEffect or shader is involved, so
 * long lists scroll at the cost of ordinary cards.
 */
internal fun Modifier.liteGlassSurface(
    style: GlassStyle,
    spec: GlassSurfaceSpec,
    shape: Shape,
    surfaceColor: Color,
    tint: Color,
    layerBlock: (GraphicsLayerScope.() -> Unit)?,
): Modifier {
    val top = if (tint.isSpecified) tint.copy(alpha = GlassLiteTokens.TINT_TOP_ALPHA) else surfaceColor
    val bottom = if (tint.isSpecified) {
        tint.copy(alpha = GlassLiteTokens.TINT_BOTTOM_ALPHA)
    } else {
        surfaceColor.copy(alpha = surfaceColor.alpha * GlassLiteTokens.FILL_BOTTOM_FACTOR)
    }
    val fill = Brush.verticalGradient(listOf(top, bottom))
    val sheen = spec.sheenBrush(style.isDark)
    return this
        .glassSurfaceLayer(shape, spec.fallbackElevation, layerBlock)
        .drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            val rim = Brush.verticalGradient(style.liteRimColors)
            // Twice the width: the outer half falls outside the clip, leaving an inner rim.
            val rimStroke = Stroke(width = GlassLiteTokens.RimWidth.toPx() * 2f)
            onDrawBehind {
                drawRect(fill)
                if (sheen != null) drawRect(sheen)
                drawOutline(outline, rim, style = rimStroke)
            }
        }
}
// ===== END SECTION: GLASS_LITE_SURFACE =====

// ===== SECTION: GLASS_LITE_RIM =====
/** The Lite rim alone, drawn on top of a blurred backdrop surface (Lite's prominent surfaces). */
internal fun Modifier.liteGlassRim(style: GlassStyle, shape: Shape): Modifier =
    drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val rim = Brush.verticalGradient(style.liteRimColors)
        val rimStroke = Stroke(width = GlassLiteTokens.RimWidth.toPx() * 2f)
        onDrawBehind { drawOutline(outline, rim, style = rimStroke) }
    }
// ===== END SECTION: GLASS_LITE_RIM =====

// ===== SECTION: GLASS_SURFACE_LAYER =====
/**
 * A single graphics layer that clips to [shape], applies [layerBlock] and casts a hardware
 * elevation shadow (cheap and cached by the renderer, unlike a blurred shadow layer).
 */
internal fun Modifier.glassSurfaceLayer(
    shape: Shape,
    elevation: Dp,
    layerBlock: (GraphicsLayerScope.() -> Unit)?,
    clip: Boolean = true,
): Modifier = graphicsLayer {
    layerBlock?.invoke(this)
    if (elevation > 0.dp) {
        shadowElevation = elevation.toPx()
        ambientShadowColor = LiteAmbientShadow
        spotShadowColor = LiteSpotShadow
    }
    this.shape = shape
    this.clip = clip
}

private val LiteAmbientShadow = Color.Black.copy(alpha = 0.10f)
private val LiteSpotShadow = Color.Black.copy(alpha = 0.14f)
// ===== END SECTION: GLASS_SURFACE_LAYER =====
