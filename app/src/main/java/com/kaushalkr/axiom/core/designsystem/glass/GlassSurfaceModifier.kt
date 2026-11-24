package com.kaushalkr.axiom.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow

// ===== SECTION: GLASS_REFRACTION_SPEC =====
/** Lens parameters passed to the Backdrop `lens` effect. Used only in Full glass. */
@Immutable
data class GlassRefraction(
    val height: Dp,
    val amount: Dp,
    val depthEffect: Boolean = false,
)
// ===== END SECTION: GLASS_REFRACTION_SPEC =====

// ===== SECTION: GLASS_SURFACE_MODIFIER =====
/**
 * Draws a Liquid Glass surface using the Kyant0 Backdrop renderer, or a solid fallback.
 *
 * - Full: vibrancy + blur + lens refraction + edge highlight + shadow.
 * - Lite: vibrancy + stronger blur + edge highlight + shadow (no RuntimeShader lens).
 * - Off: opaque surface, hairline border and an optional elevation shadow.
 *
 * @param shape must be a Kyant shape (`Capsule`, `RoundedRectangle`) or a corner-based shape,
 *   because the lens shader needs corner radii.
 * @param tint optional colour wash (for accent buttons), applied like the Backdrop catalog.
 * @param exportedBackdrop receives this surface so children can render glass-on-glass.
 * @param fallbackElevation shadow elevation used only when glass is off.
 */
fun Modifier.glassSurface(
    backdrop: Backdrop,
    style: GlassStyle,
    shape: Shape,
    blurRadius: Dp,
    refraction: GlassRefraction?,
    surfaceColor: Color = style.surfaceColor,
    tint: Color = Color.Unspecified,
    shadow: Shadow? = Shadow.Default,
    exportedBackdrop: LayerBackdrop? = null,
    fallbackElevation: Dp = 0.dp,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
): Modifier {
    if (!style.isEnabled) {
        return this
            .then(if (layerBlock != null) Modifier.graphicsLayer(layerBlock) else Modifier)
            .then(
                if (fallbackElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = fallbackElevation,
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

    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur((blurRadius * style.blurScale).toPx())
            if (style.usesRefraction && refraction != null) {
                lens(
                    refraction.height.toPx(),
                    refraction.amount.toPx(),
                    depthEffect = refraction.depthEffect,
                )
            }
        },
        highlight = { Highlight.Default },
        shadow = shadow?.let { value -> { value } },
        layerBlock = layerBlock,
        exportedBackdrop = exportedBackdrop,
        onDrawSurface = {
            if (tint.isSpecified) {
                drawRect(tint, blendMode = BlendMode.Hue)
                drawRect(tint.copy(alpha = 0.75f))
            } else {
                drawRect(surfaceColor)
            }
        },
    )
}
// ===== END SECTION: GLASS_SURFACE_MODIFIER =====
