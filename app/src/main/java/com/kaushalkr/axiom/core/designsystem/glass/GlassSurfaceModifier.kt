package com.kaushalkr.axiom.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kaushalkr.axiom.core.designsystem.glass.shader.cachedLens

// ===== SECTION: GLASS_SURFACE_MODIFIER =====
/**
 * Draws a Liquid Glass surface described by [spec] with the Kyant0 Backdrop renderer, or a
 * solid fallback when glass is off.
 *
 * - Full: saturation + blur + lens refraction (rim, depth, optional dispersion) + rim light
 *   + reflection sheen + inner depth shadow + drop shadow.
 * - Lite: saturation + stronger blur + rim light + sheen + drop shadow (no RuntimeShader lens
 *   or inner shadow).
 * - Off: opaque surface, hairline border and an optional elevation shadow.
 *
 * @param shape must be a Kyant shape (`Capsule`, `RoundedRectangle`) or a corner-based shape,
 *   because the lens shader needs corner radii.
 * @param tint optional colour wash (for accent buttons), applied like the Backdrop catalog.
 * @param exportedBackdrop receives this surface so children can render glass-on-glass.
 */
fun Modifier.glassSurface(
    backdrop: Backdrop,
    style: GlassStyle,
    spec: GlassSurfaceSpec,
    shape: Shape,
    surfaceColor: Color = style.surfaceColor,
    tint: Color = Color.Unspecified,
    exportedBackdrop: LayerBackdrop? = null,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
): Modifier {
    // ===== SECTION: GLASS_SURFACE_SOLID_FALLBACK =====
    if (!style.isEnabled) {
        return this
            .then(if (layerBlock != null) Modifier.graphicsLayer(layerBlock) else Modifier)
            .then(
                if (spec.fallbackElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = spec.fallbackElevation,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.10f),
                        spotColor = Color.Black.copy(alpha = 0.14f),
                    )
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(if (tint.isSpecified) tint else style.solidSurfaceColor)
            .border(1.dp, style.solidBorderColor, shape)
    }
    // ===== END SECTION: GLASS_SURFACE_SOLID_FALLBACK =====

    // ===== SECTION: GLASS_SURFACE_BACKDROP_RENDER =====
    val highlight = spec.edgeLight.toHighlight()
    val innerShadow = if (style.usesRefraction) spec.depth?.toInnerShadow(style.isDark) else null
    val sheenBrush = spec.sheenBrush(style.isDark)
    val refraction = if (style.usesRefraction) spec.refraction else null

    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            glassColorControls(style.isDark)
            blur((spec.blurRadius * style.blurScale).toPx())
            if (refraction != null) {
                cachedLens(
                    refraction.height.toPx(),
                    refraction.amount.toPx(),
                    depthEffect = refraction.depthEffect,
                    chromaticAberration = refraction.chromaticAberration,
                )
            }
        },
        highlight = { highlight },
        shadow = spec.shadow?.let { value -> { value } },
        innerShadow = innerShadow?.let { value -> { value } },
        layerBlock = layerBlock,
        exportedBackdrop = exportedBackdrop,
        onDrawSurface = {
            if (tint.isSpecified) {
                drawRect(tint, blendMode = BlendMode.Hue)
                drawRect(tint.copy(alpha = 0.75f))
            } else {
                drawRect(surfaceColor)
            }
            if (sheenBrush != null) drawRect(sheenBrush)
        },
    )
    // ===== END SECTION: GLASS_SURFACE_BACKDROP_RENDER =====
}
// ===== END SECTION: GLASS_SURFACE_MODIFIER =====
