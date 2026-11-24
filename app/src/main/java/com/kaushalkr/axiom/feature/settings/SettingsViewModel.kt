package com.kaushalkr.axiom.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.appearance.AppearanceRepository
import com.kaushalkr.axiom.core.data.study.StudyPreferencesRepository
import com.kaushalkr.axiom.core.model.AnimationScale
import com.kaushalkr.axiom.core.model.AppearanceSettings
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.model.StudyPreferences
import com.kaushalkr.axiom.core.model.ThemeMode
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ===== SECTION: SETTINGS_VIEW_MODEL =====
/** Exposes appearance and study preferences to the Settings screen and persists user changes. */
class SettingsViewModel(
    private val appearanceRepository: AppearanceRepository,
    private val studyPreferencesRepository: StudyPreferencesRepository,
) : ViewModel() {

    /** Null until DataStore delivers the first value. */
    val settings: StateFlow<AppearanceSettings?> = appearanceRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null,
        )

    /** Null until DataStore delivers the first value. */
    val study: StateFlow<StudyPreferences?> = studyPreferencesRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null,
        )

    // ===== SECTION: SETTINGS_VIEW_MODEL_ACTIONS =====
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { appearanceRepository.setThemeMode(themeMode) }
    }

    fun setGlassMode(glassMode: GlassMode) {
        viewModelScope.launch { appearanceRepository.setGlassMode(glassMode) }
    }

    fun useRecommendedGlassMode() {
        viewModelScope.launch { appearanceRepository.useRecommendedGlassMode() }
    }

    /** Saved on every edit; each write finishes even if the user leaves Settings meanwhile. */
    fun setUserName(name: String) {
        viewModelScope.launch { withContext(NonCancellable) { studyPreferencesRepository.setUserName(name) } }
    }

    fun setDailyGoalMinutes(minutes: Int) {
        viewModelScope.launch { studyPreferencesRepository.setDailyGoalMinutes(minutes) }
    }

    fun setGlassTintStrength(strength: Float) {
        viewModelScope.launch { appearanceRepository.setGlassTintStrength(strength) }
    }

    fun setAdaptGlassToBatterySaver(enabled: Boolean) {
        viewModelScope.launch { appearanceRepository.setAdaptGlassToBatterySaver(enabled) }
    }

    fun setAnimationScale(animationScale: AnimationScale) {
        viewModelScope.launch { appearanceRepository.setAnimationScale(animationScale) }
    }

    fun resetAppearance() {
        viewModelScope.launch { appearanceRepository.resetAppearance() }
    }
    // ===== END SECTION: SETTINGS_VIEW_MODEL_ACTIONS =====

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY]) as AxiomApplication
                SettingsViewModel(
                    appearanceRepository = application.container.appearanceRepository,
                    studyPreferencesRepository = application.container.studyPreferencesRepository,
                )
            }
        }
    }
}
// ===== END SECTION: SETTINGS_VIEW_MODEL =====
