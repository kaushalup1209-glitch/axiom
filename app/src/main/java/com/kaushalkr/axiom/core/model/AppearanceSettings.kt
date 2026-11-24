package com.kaushalkr.axiom.core.model

// ===== SECTION: MODEL_THEME_MODE =====
/** User-selected colour theme. [SYSTEM] follows the device dark-theme setting. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
// ===== END SECTION: MODEL_THEME_MODE =====

// ===== SECTION: MODEL_GLASS_MODE =====
/**
 * Liquid Glass rendering level, ordered from the most to the least expensive.
 *
 * - [FULL]: lens refraction, blur, vibrancy, edge highlights and interactive light.
 * - [LITE]: blur, vibrancy and edge highlights without refraction shaders.
 * - [OFF]: solid surfaces; no backdrop is captured or rendered.
 */
enum class GlassMode {
    FULL,
    LITE,
    OFF,
}
// ===== END SECTION: MODEL_GLASS_MODE =====

// ===== SECTION: MODEL_APPEARANCE_SETTINGS =====
/** Persisted appearance preferences. Defaults are the values applied on first launch and on reset. */
data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val glassMode: GlassMode = GlassMode.FULL,
    /** Surface tint strength in the range 0..1. Higher values improve text contrast on glass. */
    val glassTintStrength: Float = DEFAULT_GLASS_TINT_STRENGTH,
    /** When true, Battery Saver caps rendering at [GlassMode.LITE]. */
    val adaptGlassToBatterySaver: Boolean = true,
) {
    companion object {
        const val DEFAULT_GLASS_TINT_STRENGTH = 0.5f
    }
}
// ===== END SECTION: MODEL_APPEARANCE_SETTINGS =====
