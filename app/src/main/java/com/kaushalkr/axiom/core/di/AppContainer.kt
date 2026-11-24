package com.kaushalkr.axiom.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.kaushalkr.axiom.core.data.appearance.AppearanceRepository
import com.kaushalkr.axiom.core.platform.GlassDeviceCapability
import com.kaushalkr.axiom.core.platform.PowerSaveModeMonitor

// ===== SECTION: APP_CONTAINER_DATASTORE =====
private val Context.appearanceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppearanceRepository.DATASTORE_NAME,
)
// ===== END SECTION: APP_CONTAINER_DATASTORE =====

// ===== SECTION: APP_CONTAINER =====
/**
 * Manual dependency container created once by [com.kaushalkr.axiom.AxiomApplication].
 * Add new app-wide singletons here as later phases introduce them (Room database, repositories).
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val appearanceRepository: AppearanceRepository by lazy {
        AppearanceRepository(appContext.appearanceDataStore)
    }

    val powerSaveModeMonitor: PowerSaveModeMonitor by lazy {
        PowerSaveModeMonitor(appContext)
    }

    val glassDeviceCapability: GlassDeviceCapability by lazy {
        GlassDeviceCapability.from(appContext)
    }
}
// ===== END SECTION: APP_CONTAINER =====
