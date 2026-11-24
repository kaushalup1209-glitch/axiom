package com.kaushalkr.axiom

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaushalkr.axiom.app.AxiomApp
import com.kaushalkr.axiom.app.MainActivityUiState
import com.kaushalkr.axiom.app.MainActivityViewModel
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.ThemeMode

// ===== SECTION: MAIN_ACTIVITY =====
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels { MainActivityViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        holdFirstFrameUntilAppearanceLoaded()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val readyState = uiState as? MainActivityUiState.Ready
            if (readyState != null) {
                val darkTheme = shouldUseDarkTheme(readyState.settings.themeMode)
                SystemBarAppearanceEffect(darkTheme)
                AxiomTheme(
                    darkTheme = darkTheme,
                    glassResolution = readyState.glassResolution,
                    glassTintStrength = readyState.settings.glassTintStrength,
                    animationScale = readyState.settings.animationScale,
                ) {
                    AxiomApp()
                }
            }
        }
    }

    // ===== SECTION: MAIN_ACTIVITY_FIRST_FRAME_GATE =====
    /**
     * Keeps the window background (and the system splash on Android 12+) visible until the
     * stored theme is known, so a user-selected theme never flashes the wrong colours.
     */
    private fun holdFirstFrameUntilAppearanceLoaded() {
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    val isReady = viewModel.uiState.value is MainActivityUiState.Ready
                    if (isReady) content.viewTreeObserver.removeOnPreDrawListener(this)
                    return isReady
                }
            },
        )
    }
    // ===== END SECTION: MAIN_ACTIVITY_FIRST_FRAME_GATE =====

    // ===== SECTION: MAIN_ACTIVITY_SYSTEM_BARS =====
    /** Matches status and navigation bar icon colours to the in-app theme. */
    @Composable
    private fun SystemBarAppearanceEffect(darkTheme: Boolean) {
        DisposableEffect(darkTheme) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                navigationBarStyle = SystemBarStyle.auto(LightNavigationScrim, DarkNavigationScrim) { darkTheme },
            )
            onDispose {}
        }
    }
    // ===== END SECTION: MAIN_ACTIVITY_SYSTEM_BARS =====

    private companion object {
        /** Scrims used only by three-button navigation; gesture navigation stays transparent. */
        val LightNavigationScrim = Color.argb(0x66, 0xF3, 0xF5, 0xFA)
        val DarkNavigationScrim = Color.argb(0x66, 0x09, 0x0C, 0x13)
    }
}
// ===== END SECTION: MAIN_ACTIVITY =====

// ===== SECTION: THEME_MODE_RESOLUTION =====
@Composable
private fun shouldUseDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
// ===== END SECTION: THEME_MODE_RESOLUTION =====
