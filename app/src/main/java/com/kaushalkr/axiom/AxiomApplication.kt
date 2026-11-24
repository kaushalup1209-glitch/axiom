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
    }

    /**
     * Compiles the shared Full-glass shaders off the main thread. Called only once Full glass is
     * in effect: Lite and Off never use a RuntimeShader, so they skip the work entirely.
     */
    fun prewarmGlassShaders() {
        applicationScope.launch { GlassShaderCache.prewarm() }
    }
}
// ===== END SECTION: AXIOM_APPLICATION =====
