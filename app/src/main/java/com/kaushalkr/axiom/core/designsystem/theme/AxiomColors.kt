package com.kaushalkr.axiom.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ===== SECTION: AXIOM_COLORS_MODEL =====
/** Semantic colour tokens. Components read these instead of hard-coded colours. */
@Immutable
data class AxiomColors(
    val isDark: Boolean,
    val background: Color,
    val backgroundGlowPrimary: Color,
    val backgroundGlowSecondary: Color,
    val backgroundGlowTertiary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val danger: Color,
    /** Base colour of glass surfaces; alpha is applied by the glass style from the tint setting. */
    val glassSurface: Color,
    /** Opaque surface used when glass rendering is off. */
    val solidSurface: Color,
    val solidSurfaceBorder: Color,
    val controlTrack: Color,
    val controlThumb: Color,
    /** Resting fill of the selected segment in segmented controls. */
    val segmentIndicator: Color,
    val divider: Color,
    val scrim: Color,
)
// ===== END SECTION: AXIOM_COLORS_MODEL =====

// ===== SECTION: AXIOM_COLORS_LIGHT =====
val LightAxiomColors = AxiomColors(
    isDark = false,
    background = Color(0xFFF3F5FA),
    backgroundGlowPrimary = Color(0xFF8B8CFF),
    backgroundGlowSecondary = Color(0xFF5EE3F5),
    backgroundGlowTertiary = Color(0xFFF7A6D2),
    textPrimary = Color(0xFF0E1117),
    textSecondary = Color(0xFF4A5260),
    textTertiary = Color(0xFF737B8A),
    accent = Color(0xFF4F46E5),
    accentSoft = Color(0x1F4F46E5),
    onAccent = Color(0xFFFFFFFF),
    danger = Color(0xFFD92D20),
    glassSurface = Color(0xFFFFFFFF),
    solidSurface = Color(0xFFFFFFFF),
    solidSurfaceBorder = Color(0x140E1117),
    controlTrack = Color(0x33787880),
    controlThumb = Color(0xFFFFFFFF),
    segmentIndicator = Color(0xEBFFFFFF),
    divider = Color(0x140E1117),
    scrim = Color(0x47202238),
)
// ===== END SECTION: AXIOM_COLORS_LIGHT =====

// ===== SECTION: AXIOM_COLORS_DARK =====
val DarkAxiomColors = AxiomColors(
    isDark = true,
    background = Color(0xFF090C13),
    backgroundGlowPrimary = Color(0xFF4F46E5),
    backgroundGlowSecondary = Color(0xFF0E7490),
    backgroundGlowTertiary = Color(0xFF7C3AED),
    textPrimary = Color(0xFFF3F5F9),
    textSecondary = Color(0xFFAAB1BE),
    textTertiary = Color(0xFF7D8594),
    accent = Color(0xFF908EFF),
    accentSoft = Color(0x29908EFF),
    onAccent = Color(0xFF0D0B26),
    danger = Color(0xFFFF6B5E),
    glassSurface = Color(0xFF1A1E29),
    solidSurface = Color(0xFF151922),
    solidSurfaceBorder = Color(0x14FFFFFF),
    controlTrack = Color(0x5C787880),
    controlThumb = Color(0xFFFFFFFF),
    segmentIndicator = Color(0x2EFFFFFF),
    divider = Color(0x14FFFFFF),
    scrim = Color(0x80000000),
)
// ===== END SECTION: AXIOM_COLORS_DARK =====

// ===== SECTION: AXIOM_COLORS_LOCAL =====
val LocalAxiomColors = staticCompositionLocalOf { LightAxiomColors }
// ===== END SECTION: AXIOM_COLORS_LOCAL =====

// ===== SECTION: AXIOM_COLORS_MATERIAL_BRIDGE =====
/** Maps Axiom tokens onto Material 3 so Material text and indication defaults stay consistent. */
internal fun AxiomColors.toMaterialColorScheme(): ColorScheme =
    if (isDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accentSoft,
            background = background,
            onBackground = textPrimary,
            surface = solidSurface,
            onSurface = textPrimary,
            onSurfaceVariant = textSecondary,
            outlineVariant = divider,
            error = danger,
            scrim = scrim,
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accentSoft,
            background = background,
            onBackground = textPrimary,
            surface = solidSurface,
            onSurface = textPrimary,
            onSurfaceVariant = textSecondary,
            outlineVariant = divider,
            error = danger,
            scrim = scrim,
        )
    }
// ===== END SECTION: AXIOM_COLORS_MATERIAL_BRIDGE =====
