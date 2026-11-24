package com.kaushalkr.axiom.testing

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.test.core.app.ApplicationProvider
import com.kaushalkr.axiom.app.AxiomApp
import com.kaushalkr.axiom.core.designsystem.glass.GlassLimitation
import com.kaushalkr.axiom.core.designsystem.glass.GlassResolution
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.GlassMode

// ===== SECTION: TEST_HARNESS =====
/** Renders the full app with a fixed theme and glass level, bypassing device detection. */
@Composable
fun AxiomTestApp(
    glassMode: GlassMode,
    darkTheme: Boolean = false,
) {
    AxiomTheme(
        darkTheme = darkTheme,
        glassResolution = GlassResolution(glassMode, glassMode, GlassLimitation.NONE),
    ) {
        AxiomApp()
    }
}

fun string(@StringRes id: Int): String =
    ApplicationProvider.getApplicationContext<Context>().getString(id)
// ===== END SECTION: TEST_HARNESS =====
