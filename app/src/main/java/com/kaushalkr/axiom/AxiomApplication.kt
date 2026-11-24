package com.kaushalkr.axiom

import android.app.Application
import com.kaushalkr.axiom.core.di.AppContainer

// ===== SECTION: AXIOM_APPLICATION =====
class AxiomApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
// ===== END SECTION: AXIOM_APPLICATION =====
