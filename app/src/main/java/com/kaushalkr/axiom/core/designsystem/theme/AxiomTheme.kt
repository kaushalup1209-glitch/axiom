package com.kaushalkr.axiom.core.designsystem.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.kaushalkr.axiom.core.designsystem.glass.GlassResolution
import com.kaushalkr.axiom.core.designsystem.glass.GlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassResolution
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotion
import com.kaushalkr.axiom.core.designsystem.motion.AxiomPressIndication
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.model.AnimationScale
import com.kaushalkr.axiom.core.model.AppearanceSettings

// ===== SECTION: AXIOM_THEME =====
/**
 * Root theme. Provides colour tokens, typography, the resolved Liquid Glass style, the
 * motion scheme for the chosen animation scale, and a soft press indication in place of
 * the Material ripple.
 *
 * @param darkTheme whether the dark palette is used.
 * @param glassResolution requested and effective glass level after device and power limits.
 * @param glassTintStrength user tint preference in 0..1.
 * @param animationScale user animation duration multiplier.
 */
@Composable
fun AxiomTheme(
    darkTheme: Boolean,
    glassResolution: GlassResolution = GlassResolution.Default,
    glassTintStrength: Float = AppearanceSettings.DEFAULT_GLASS_TINT_STRENGTH,
    animationScale: AnimationScale = AnimationScale.NORMAL,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAxiomColors else LightAxiomColors
    val colorScheme = remember(colors) { colors.toMaterialColorScheme() }
    val glassStyle = remember(glassResolution.effective, glassTintStrength, colors) {
        GlassStyle.create(glassResolution.effective, glassTintStrength, colors)
    }
    val motion = remember(animationScale) { AxiomMotion(animationScale.factor) }
    val pressIndication = remember(colors, motion) { AxiomPressIndication(colors.pressOverlay, motion) }

    CompositionLocalProvider(
        LocalAxiomColors provides colors,
        LocalGlassStyle provides glassStyle,
        LocalGlassResolution provides glassResolution,
        LocalAxiomMotion provides motion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AxiomTypography,
        ) {
            // Provided inside MaterialTheme so it replaces the Material ripple app-wide.
            CompositionLocalProvider(LocalIndication provides pressIndication, content = content)
        }
    }
}
// ===== END SECTION: AXIOM_THEME =====

// ===== SECTION: AXIOM_THEME_ACCESSORS =====
/** Convenience accessors, e.g. `AxiomTheme.colors.textPrimary`. */
object AxiomTheme {
    val colors: AxiomColors
        @Composable @ReadOnlyComposable get() = LocalAxiomColors.current

    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography

    val glass: GlassStyle
        @Composable @ReadOnlyComposable get() = LocalGlassStyle.current

    val motion: AxiomMotion
        @Composable @ReadOnlyComposable get() = LocalAxiomMotion.current
}
// ===== END SECTION: AXIOM_THEME_ACCESSORS =====
