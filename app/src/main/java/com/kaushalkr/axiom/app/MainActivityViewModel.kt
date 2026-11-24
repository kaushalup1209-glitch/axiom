package com.kaushalkr.axiom.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.appearance.AppearanceRepository
import com.kaushalkr.axiom.core.designsystem.glass.GlassModeResolver
import com.kaushalkr.axiom.core.designsystem.glass.GlassResolution
import com.kaushalkr.axiom.core.model.AppearanceSettings
import com.kaushalkr.axiom.core.platform.GlassDeviceCapability
import com.kaushalkr.axiom.core.platform.PowerSaveModeMonitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// ===== SECTION: MAIN_ACTIVITY_UI_STATE =====
sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState

    data class Ready(
        val settings: AppearanceSettings,
        val glassResolution: GlassResolution,
    ) : MainActivityUiState
}
// ===== END SECTION: MAIN_ACTIVITY_UI_STATE =====

// ===== SECTION: MAIN_ACTIVITY_VIEW_MODEL =====
/**
 * App-wide appearance state: persisted preferences combined with device capability and
 * Battery Saver to produce the glass level that is actually rendered.
 */
class MainActivityViewModel(
    appearanceRepository: AppearanceRepository,
    powerSaveModeMonitor: PowerSaveModeMonitor,
    glassDeviceCapability: GlassDeviceCapability,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = combine(
        appearanceRepository.settings,
        powerSaveModeMonitor.isPowerSaveMode,
    ) { settings, isPowerSaveMode ->
        MainActivityUiState.Ready(
            settings = settings,
            glassResolution = GlassModeResolver.resolve(
                requested = settings.glassMode,
                capability = glassDeviceCapability,
                isPowerSaveMode = isPowerSaveMode,
                adaptToBatterySaver = settings.adaptGlassToBatterySaver,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MainActivityUiState.Loading,
    )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                MainActivityViewModel(
                    appearanceRepository = container.appearanceRepository,
                    powerSaveModeMonitor = container.powerSaveModeMonitor,
                    glassDeviceCapability = container.glassDeviceCapability,
                )
            }
        }
    }
}
// ===== END SECTION: MAIN_ACTIVITY_VIEW_MODEL =====
