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
 * Per-surface values live in [GlassTokens]; tint ranges in [GlassTintTokens].
 */
@Immutable
data class GlassStyle(
    val mode: GlassMode,
    val isDark: Boolean,
    val tintStrength: Float,
    /** Tint drawn over the backdrop of cards and dialogs. */
    val surfaceColor: Color,
    /** Tint drawn over the backdrop of floating chrome (navigation bar, top buttons). */
    val chromeSurfaceColor: Color,
    /** Opaque surface used in [GlassMode.OFF]. */
    val solidSurfaceColor: Color,
    val solidBorderColor: Color,
) {
    /** True when backdrops are captured and drawn at all. */
    val isEnabled: Boolean get() = mode != GlassMode.OFF

    /** Lens refraction, dispersion and inner depth shadows (RuntimeShader). */
    val usesRefraction: Boolean get() = mode == GlassMode.FULL

    /** Pointer-following light shader on press. */
    val usesInteractiveLight: Boolean get() = mode == GlassMode.FULL

    val blurScale: Float
        get() = when (mode) {
            GlassMode.FULL -> 1f
            GlassMode.LITE -> GlassTintTokens.LITE_BLUR_SCALE
            GlassMode.OFF -> 0f
        }

    /** Dialog tint: the card tint, raised to a readable minimum. */
    val dialogSurfaceColor: Color
        get() = surfaceColor.copy(alpha = maxOf(surfaceColor.alpha, GlassTintTokens.DIALOG_MIN_ALPHA))

    companion object {
        fun create(mode: GlassMode, tintStrength: Float, colors: AxiomColors): GlassStyle {
            val strength = tintStrength.coerceIn(0f, 1f)
            val surfaceRange = GlassTintTokens.surfaceAlphaRange(mode)
            val chromeRange = GlassTintTokens.chromeAlphaRange(mode)
            return GlassStyle(
                mode = mode,
                isDark = colors.isDark,
                tintStrength = strength,
                surfaceColor = colors.glassSurface.copy(
                    alpha = lerp(surfaceRange.start, surfaceRange.endInclusive, strength),
                ),
                chromeSurfaceColor = colors.glassSurface.copy(
                    alpha = lerp(chromeRange.start, chromeRange.endInclusive, strength),
                ),
                solidSurfaceColor = colors.solidSurface,
                solidBorderColor = colors.solidSurfaceBorder,
            )
        }
    }
}
// ===== END SECTION: GLASS_STYLE_MODEL =====

// ===== SECTION: GLASS_STYLE_LOCALS =====
val LocalGlassStyle = staticCompositionLocalOf {
    GlassStyle.create(GlassMode.FULL, AppearanceSettings.DEFAULT_GLASS_TINT_STRENGTH, LightAxiomColors)
}

/** The requested and effective glass level, exposed so Settings can explain any limitation. */
val LocalGlassResolution = staticCompositionLocalOf { GlassResolution.Default }
// ===== END SECTION: GLASS_STYLE_LOCALS =====
