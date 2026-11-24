package com.kaushalkr.axiom.core.data.appearance

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kaushalkr.axiom.core.model.AnimationScale
import com.kaushalkr.axiom.core.model.AppearanceSettings
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Single source of truth for appearance preferences, backed by Preferences DataStore.
 * Reads and writes are main-safe; DataStore performs disk I/O on its own dispatcher.
 */
class AppearanceRepository(
    private val dataStore: DataStore<Preferences>,
) {

    // ===== SECTION: APPEARANCE_REPOSITORY_READ =====
    val settings: Flow<AppearanceSettings> = dataStore.data
        .catch { error ->
            // A corrupted or unreadable file falls back to defaults instead of crashing the app.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences.toAppearanceSettings() }
        .distinctUntilChanged()
    // ===== END SECTION: APPEARANCE_REPOSITORY_READ =====

    // ===== SECTION: APPEARANCE_REPOSITORY_WRITE =====
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun setGlassMode(glassMode: GlassMode) {
        dataStore.edit { it[Keys.GLASS_MODE] = glassMode.name }
    }

    /** Clears the manual choice so the device recommendation applies again. */
    suspend fun useRecommendedGlassMode() {
        dataStore.edit { it.remove(Keys.GLASS_MODE) }
    }

    suspend fun setGlassTintStrength(strength: Float) {
        dataStore.edit { it[Keys.GLASS_TINT_STRENGTH] = strength.coerceIn(0f, 1f) }
    }

    suspend fun setAdaptGlassToBatterySaver(enabled: Boolean) {
        dataStore.edit { it[Keys.ADAPT_TO_BATTERY_SAVER] = enabled }
    }

    suspend fun setAnimationScale(animationScale: AnimationScale) {
        dataStore.edit { it[Keys.ANIMATION_SCALE] = animationScale.name }
    }

    suspend fun resetAppearance() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.THEME_MODE)
            preferences.remove(Keys.GLASS_MODE)
            preferences.remove(Keys.GLASS_TINT_STRENGTH)
            preferences.remove(Keys.ADAPT_TO_BATTERY_SAVER)
            preferences.remove(Keys.ANIMATION_SCALE)
        }
    }
    // ===== END SECTION: APPEARANCE_REPOSITORY_WRITE =====

    // ===== SECTION: APPEARANCE_REPOSITORY_MAPPING =====
    private fun Preferences.toAppearanceSettings(): AppearanceSettings {
        val defaults = AppearanceSettings()
        return AppearanceSettings(
            themeMode = enumValueOrDefault(this[Keys.THEME_MODE], defaults.themeMode),
            glassMode = enumValues<GlassMode>().firstOrNull { it.name == this[Keys.GLASS_MODE] },
            glassTintStrength = (this[Keys.GLASS_TINT_STRENGTH] ?: defaults.glassTintStrength).coerceIn(0f, 1f),
            adaptGlassToBatterySaver = this[Keys.ADAPT_TO_BATTERY_SAVER] ?: defaults.adaptGlassToBatterySaver,
            animationScale = enumValueOrDefault(this[Keys.ANIMATION_SCALE], defaults.animationScale),
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(storedName: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == storedName } ?: default

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GLASS_MODE = stringPreferencesKey("glass_mode")
        val GLASS_TINT_STRENGTH = floatPreferencesKey("glass_tint_strength")
        val ADAPT_TO_BATTERY_SAVER = booleanPreferencesKey("adapt_glass_to_battery_saver")
        val ANIMATION_SCALE = stringPreferencesKey("animation_scale")
    }
    // ===== END SECTION: APPEARANCE_REPOSITORY_MAPPING =====

    companion object {
        /** DataStore file name. Must match the backup rules in res/xml. */
        const val DATASTORE_NAME = "axiom_appearance"
    }
}
