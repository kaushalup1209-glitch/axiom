package com.kaushalkr.axiom.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.testing.AxiomTestApp
import com.kaushalkr.axiom.testing.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

// ===== SECTION: TEST_SCREENSHOT_CAPTURE =====
/**
 * Renders key screens with Robolectric native graphics and writes PNGs to
 * app/build/screenshots for manual layout review. It does not assert pixels.
 * Robolectric renders on the CPU, so blur and refraction shaders may not appear;
 * use a device or emulator to judge the real Liquid Glass effect.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-xxhdpi")
class AxiomScreenshotCaptureTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val directory = File("build/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    @Test
    fun home_light_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        capture("home_light_full")
    }

    @Test
    fun home_dark_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL, darkTheme = true) }
        capture("home_dark_full")
    }

    @Test
    fun home_light_off() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.OFF) }
        capture("home_light_off")
    }

    @Test
    fun settings_light_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_glass)), timeoutMillis = 5_000)
        capture("settings_light_full")
    }

    @Test
    fun settings_dark_off() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.OFF, darkTheme = true) }
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_glass)), timeoutMillis = 5_000)
        capture("settings_dark_off")
    }

    @Test
    @Config(qualifiers = "w840dp-h1200dp-xhdpi")
    fun home_tablet_light_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        capture("home_tablet_light_full")
    }
}
// ===== END SECTION: TEST_SCREENSHOT_CAPTURE =====
