package com.kaushalkr.axiom.core.designsystem.glass

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.lerp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomColors
import com.kaushalkr.axiom.core.designsystem.theme.LightAxiomColors
import com.kaushalkr.axiom.core.model.AppearanceSettings
import com.kaushalkr.axiom.core.model.GlassMode

// ===== SECTION: GLASS_STYLE_MODEL =====
/**
 * Rendering parameters for the effective [GlassMode]. Every glass component reads this
 * through [LocalGlassStyle], so switching Full, Lite and Off needs no per-screen code.
 * Per-surface values live in [GlassTokens]; tint ranges in [GlassTintTokens]; Lite's
 * lightweight glass in [GlassLiteTokens].
 *
 * - Full: the original Kyant0 Liquid Glass on every surface.
 * - Lite: lightweight glass (gradient, sheen, rim) on standard surfaces; backdrop blur only on
 *   [GlassEmphasis.PROMINENT] surfaces, and only when [allowsBackdropBlur].
 * - Off: solid surfaces; nothing is captured, blurred or shaded.
 */
@Immutable
data class GlassStyle(
    val mode: GlassMode,
    val isDark: Boolean,
    val tintStrength: Float,
    /** Tint of cards, controls and dialogs. */
    val surfaceColor: Color,
    /** Tint of floating chrome (navigation bar, top buttons, undo bar). */
    val chromeSurfaceColor: Color,
    /** Tint of Lite chrome that sits over a blurred backdrop; equals [chromeSurfaceColor] otherwise. */
    val blurredChromeSurfaceColor: Color,
    /** Opaque surface used in [GlassMode.OFF]. */
    val solidSurfaceColor: Color,
    val solidBorderColor: Color,
    /** Lite rim gradient, top to bottom. */
    val liteRimColors: List<Color>,
    /** Lite may blur behind prominent surfaces (needs RenderEffect; off under Battery Saver). */
    val allowsBackdropBlur: Boolean = true,
) {
    /** True when glass styling is drawn at all (Full or Lite). */
    val isEnabled: Boolean get() = mode != GlassMode.OFF

    /** The original Kyant0 Liquid Glass pipeline. */
    val isFull: Boolean get() = mode == GlassMode.FULL

    /** Lens refraction, dispersion and inner depth shadows (RuntimeShader). */
    val usesRefraction: Boolean get() = isFull

    /** Pointer-following light shader on press. */
    val usesInteractiveLight: Boolean get() = isFull

    val blurScale: Float
        get() = when (mode) {
            GlassMode.FULL -> 1f
            GlassMode.LITE -> GlassTintTokens.LITE_BLUR_SCALE
            GlassMode.OFF -> 0f
        }

    /** Whether a surface of [spec] samples a backdrop (blur, lens) rather than drawing lightweight glass. */
    fun usesBackdrop(spec: GlassSurfaceSpec): Boolean = when (mode) {
        GlassMode.FULL -> true
        GlassMode.LITE -> allowsBackdropBlur && spec.emphasis == GlassEmphasis.PROMINENT
        GlassMode.OFF -> false
    }

    /** The ambient background is captured only for Full in-screen glass. */
    val capturesBackgroundBackdrop: Boolean get() = isFull

    /** The whole screen is captured for chrome and overlays that blur it. */
    val capturesScreenBackdrop: Boolean
        get() = isFull || (mode == GlassMode.LITE && allowsBackdropBlur)

    /** Cards export their own surface for glass-on-glass children only in Full. */
    val exportsNestedBackdrops: Boolean get() = isFull

    /**
     * Dialog and sheet tint, raised to a readable minimum. Lite's blurred overlays use the lighter
     * blurred tint so their frost shows; without blur they keep the stronger lightweight tint.
     */
    val dialogSurfaceColor: Color
        get() {
            val base = if (mode == GlassMode.LITE && allowsBackdropBlur) blurredChromeSurfaceColor else surfaceColor
            return base.copy(alpha = maxOf(base.alpha, GlassTintTokens.DIALOG_MIN_ALPHA))
        }

    companion object {
        fun create(
            mode: GlassMode,
            tintStrength: Float,
            colors: AxiomColors,
            allowsBackdropBlur: Boolean = true,
        ): GlassStyle {
            val strength = tintStrength.coerceIn(0f, 1f)
            val surfaceRange = GlassTintTokens.surfaceAlphaRange(mode)
            val chromeRange = GlassTintTokens.chromeAlphaRange(mode)
            val chromeSurface = colors.glassSurface.copy(alpha = lerp(chromeRange.start, chromeRange.endInclusive, strength))
            val blurredRange = GlassTintTokens.LiteBlurredChromeAlphaRange
            return GlassStyle(
                mode = mode,
                isDark = colors.isDark,
                tintStrength = strength,
                surfaceColor = colors.glassSurface.copy(
                    alpha = lerp(surfaceRange.start, surfaceRange.endInclusive, strength),
                ),
                chromeSurfaceColor = chromeSurface,
                blurredChromeSurfaceColor = if (mode == GlassMode.LITE && allowsBackdropBlur) {
                    colors.glassSurface.copy(alpha = lerp(blurredRange.start, blurredRange.endInclusive, strength))
                } else {
                    chromeSurface
                },
                solidSurfaceColor = colors.solidSurface,
                solidBorderColor = colors.solidSurfaceBorder,
                liteRimColors = if (colors.isDark) GlassLiteTokens.RimDark else GlassLiteTokens.RimLight,
                allowsBackdropBlur = allowsBackdropBlur,
            )
        }
    }
}
// ===== END SECTION: GLASS_STYLE_MODEL =====

// ===== SECTION: GLASS_STYLE_LOCALS =====
val LocalGlassStyle = staticCompositionLocalOf {
    GlassStyle.create(GlassMode.LITE, AppearanceSettings.DEFAULT_GLASS_TINT_STRENGTH, LightAxiomColors)
}

/** The requested and effective glass level, exposed so Settings can explain any limitation. */
val LocalGlassResolution = staticCompositionLocalOf { GlassResolution.Default }
// ===== END SECTION: GLASS_STYLE_LOCALS =====
