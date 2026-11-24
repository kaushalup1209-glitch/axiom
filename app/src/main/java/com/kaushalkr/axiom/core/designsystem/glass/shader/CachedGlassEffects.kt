/*
 * Copyright 2025 Kyant
 * Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 *
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   backdrop/src/commonMain/kotlin/com/kyant/backdrop/effects/Lens.kt
 *   backdrop/src/commonMain/kotlin/com/kyant/backdrop/highlight/HighlightStyle.kt
 * Changes for Axiom: shaders come from the shared GlassShaderCache instead of a per-element
 * cache. Uniforms, padding handling, corner-radius rules and blending are unchanged, so the
 * rendered result is identical to the library's `lens`, `HighlightStyle.Default` and
 * `HighlightStyle.Ambient`.
 */
package com.kaushalkr.axiom.core.designsystem.glass.shader

import android.graphics.RenderEffect
import android.os.Build
import androidx.annotation.FloatRange
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceAtMost
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.RuntimeShaderCache
import com.kyant.backdrop.asAndroidRuntimeShader
import com.kyant.backdrop.effects.effect
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.isRuntimeShaderSupported
import com.kyant.shapes.RoundedRectangularShape
import kotlin.math.PI

// ===== SECTION: CACHED_LENS_EFFECT =====
/** The Backdrop `lens` effect, rendered with a shared compiled shader. */
fun BackdropEffectScope.cachedLens(
    @FloatRange(from = 0.0) refractionHeight: Float,
    @FloatRange(from = 0.0) refractionAmount: Float,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    if (refractionHeight <= 0f || refractionAmount <= 0f) return

    if (padding > 0f) {
        padding = (padding - refractionHeight).fastCoerceAtLeast(0f)
    }

    val cornerRadii = lensCornerRadii
        ?: throw UnsupportedOperationException(
            "Only RoundedRectangularShape or CornerBasedShape is supported in lens effects.",
        )
    val shader = GlassShaderCache.obtain(
        if (chromaticAberration) GlassShaderCache.Kind.REFRACTION_WITH_DISPERSION
        else GlassShaderCache.Kind.REFRACTION,
    ).apply {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform("offset", -padding, -padding)
        setFloatUniform("cornerRadii", cornerRadii)
        setFloatUniform("refractionHeight", refractionHeight)
        setFloatUniform("refractionAmount", -refractionAmount)
        setFloatUniform("depthEffect", if (depthEffect) 1f else 0f)
        if (chromaticAberration) {
            setFloatUniform("chromaticAberration", 1f)
        }
    }
    // Created immediately after the uniforms are set, so the effect captures this element's values.
    effect(RenderEffect.createRuntimeShaderEffect(shader.asAndroidRuntimeShader(), "content").asComposeRenderEffect())
}

private val BackdropEffectScope.lensCornerRadii: FloatArray?
    get() = when (val shape = shape) {
        is RoundedRectangularShape -> {
            val corners = shape.corners(size, layoutDirection, this)
            floatArrayOf(corners.topLeft, corners.topRight, corners.bottomRight, corners.bottomLeft)
        }

        is AbsoluteRoundedCornerShape -> {
            val maxRadius = size.minDimension / 2f
            floatArrayOf(
                shape.topStart.toPx(size, this).fastCoerceAtMost(maxRadius),
                shape.topEnd.toPx(size, this).fastCoerceAtMost(maxRadius),
                shape.bottomEnd.toPx(size, this).fastCoerceAtMost(maxRadius),
                shape.bottomStart.toPx(size, this).fastCoerceAtMost(maxRadius),
            )
        }

        is CornerBasedShape -> cornerBasedRadii(shape, size, layoutDirection == LayoutDirection.Ltr)

        else -> null
    }
// ===== END SECTION: CACHED_LENS_EFFECT =====

// ===== SECTION: CACHED_HIGHLIGHT_STYLES =====
/** `HighlightStyle.Default` (directional rim light) with a shared compiled shader. */
@Immutable
data class CachedDefaultHighlightStyle(
    override val color: Color = Color.White.copy(alpha = 0.5f),
    override val blendMode: BlendMode = BlendMode.Plus,
    val angle: Float = 45f,
    @param:FloatRange(from = 0.0) val falloff: Float = 1f,
) : HighlightStyle {

    override fun DrawScope.createShader(shape: Shape, runtimeShaderCache: RuntimeShaderCache): RuntimeShader? {
        if (!isRuntimeShaderSupported()) return null
        return GlassShaderCache.obtain(GlassShaderCache.Kind.DEFAULT_HIGHLIGHT).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("cornerRadii", highlightCornerRadii(shape))
            setColorUniform("color", color.copy(alpha = 1f))
            setFloatUniform("angle", angle * (PI / 180f).toFloat())
            setFloatUniform("falloff", falloff)
        }
    }
}

/** `HighlightStyle.Ambient` (soft all-round rim light) with a shared compiled shader. */
@Immutable
data class CachedAmbientHighlightStyle(
    @param:FloatRange(from = 0.0, to = 1.0) val intensity: Float = 0.38f,
) : HighlightStyle {

    override val color: Color = Color.White.copy(alpha = intensity)

    override val blendMode: BlendMode = DrawScope.DefaultBlendMode

    override fun DrawScope.createShader(shape: Shape, runtimeShaderCache: RuntimeShaderCache): RuntimeShader? {
        if (!isRuntimeShaderSupported()) return null
        return GlassShaderCache.obtain(GlassShaderCache.Kind.AMBIENT_HIGHLIGHT).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("cornerRadii", highlightCornerRadii(shape))
            setFloatUniform("angle", 45f * (PI / 180f).toFloat())
            setFloatUniform("falloff", 1f)
        }
    }
}

/** Drop-in replacements for `Highlight.Default` and `Highlight.Ambient`. */
object CachedHighlights {
    @Stable
    val Default: Highlight = Highlight(style = CachedDefaultHighlightStyle())

    @Stable
    val Ambient: Highlight = Highlight(style = CachedAmbientHighlightStyle())

    fun edge(width: Dp = 0.5.dp, intensity: Float = 0.5f, angle: Float = 45f, falloff: Float = 1f, alpha: Float = 1f): Highlight =
        Highlight(
            width = width,
            blurRadius = width / 2f,
            alpha = alpha,
            style = CachedDefaultHighlightStyle(
                color = Color.White.copy(alpha = intensity),
                angle = angle,
                falloff = falloff,
            ),
        )
}

/** Same rule as the library: only CornerBasedShape radii are read; others use a capsule radius. */
private fun DrawScope.highlightCornerRadii(shape: Shape): FloatArray {
    val cornerShape = shape as? CornerBasedShape ?: return FloatArray(4) { size.minDimension / 2f }
    return cornerBasedRadii(cornerShape, size, layoutDirection == LayoutDirection.Ltr)
}
// ===== END SECTION: CACHED_HIGHLIGHT_STYLES =====

// ===== SECTION: CACHED_CORNER_RADII =====
/** Corner radii in top-left, top-right, bottom-right, bottom-left order, capped at half the short side. */
private fun Density.cornerBasedRadii(
    shape: CornerBasedShape,
    size: Size,
    isLtr: Boolean,
): FloatArray {
    val maxRadius = size.minDimension / 2f
    val topStart = shape.topStart.toPx(size, this)
    val topEnd = shape.topEnd.toPx(size, this)
    val bottomEnd = shape.bottomEnd.toPx(size, this)
    val bottomStart = shape.bottomStart.toPx(size, this)
    return floatArrayOf(
        (if (isLtr) topStart else topEnd).fastCoerceAtMost(maxRadius),
        (if (isLtr) topEnd else topStart).fastCoerceAtMost(maxRadius),
        (if (isLtr) bottomEnd else bottomStart).fastCoerceAtMost(maxRadius),
        (if (isLtr) bottomStart else bottomEnd).fastCoerceAtMost(maxRadius),
    )
}
// ===== END SECTION: CACHED_CORNER_RADII =====
