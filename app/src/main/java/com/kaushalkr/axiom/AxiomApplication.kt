package com.kaushalkr.axiom

import android.app.Application
import com.kaushalkr.axiom.core.designsystem.glass.shader.GlassShaderCache
import com.kaushalkr.axiom.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// ===== SECTION: AXIOM_APPLICATION =====
class AxiomApplication : Application() {

    lateinit var container: AppContainer
        private set

    /** App-lifetime scope for background start-up work. */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Compile every shared glass shader off the main thread before the first glass surface draws.
        applicationScope.launch { GlassShaderCache.prewarm() }
    }
}
// ===== END SECTION: AXIOM_APPLICATION =====
