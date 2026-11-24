package com.kaushalkr.axiom.core.designsystem.glass

import com.kaushalkr.axiom.core.designsystem.theme.DarkAxiomColors
import com.kaushalkr.axiom.core.designsystem.theme.LightAxiomColors
import com.kaushalkr.axiom.core.model.GlassMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ===== SECTION: TEST_GLASS_STYLE =====
/** The rendering budget of each mode: which surfaces sample a backdrop and which layers are captured. */
class GlassStyleTest {

    private val standardSurfaces = listOf(
        GlassTokens.Card,
        GlassTokens.ListItem,
        GlassTokens.Button,
        GlassTokens.FloatingButton,
        GlassTokens.Input,
        GlassTokens.Snackbar,
        GlassTokens.SegmentedControl,
    )
    private val prominentSurfaces = listOf(GlassTokens.NavigationBar, GlassTokens.Dialog, GlassTokens.Sheet)

    private fun style(mode: GlassMode, blur: Boolean = true, dark: Boolean = false) =
        GlassStyle.create(mode, tintStrength = 0.5f, colors = if (dark) DarkAxiomColors else LightAxiomColors, allowsBackdropBlur = blur)

    @Test
    fun full_keepsTheOriginalPipelineEverywhere() {
        val full = style(GlassMode.FULL)
        (standardSurfaces + prominentSurfaces).forEach { assertTrue(full.usesBackdrop(it)) }
        assertTrue(full.capturesBackgroundBackdrop)
        assertTrue(full.capturesScreenBackdrop)
        assertTrue(full.exportsNestedBackdrops)
        assertTrue(full.usesRefraction)
        assertTrue(full.usesInteractiveLight)
    }

    @Test
    fun lite_blursOnlyProminentSurfaces_andCapturesOnlyTheScreen() {
        val lite = style(GlassMode.LITE)
        standardSurfaces.forEach { assertFalse(lite.usesBackdrop(it)) }
        prominentSurfaces.forEach { assertTrue(lite.usesBackdrop(it)) }
        assertFalse(lite.capturesBackgroundBackdrop)
        assertTrue(lite.capturesScreenBackdrop)
        assertFalse(lite.exportsNestedBackdrops)
        assertFalse(lite.usesRefraction)
        assertFalse(lite.usesInteractiveLight)
    }

    @Test
    fun lite_withoutBlur_capturesNothing() {
        val lite = style(GlassMode.LITE, blur = false)
        (standardSurfaces + prominentSurfaces).forEach { assertFalse(lite.usesBackdrop(it)) }
        assertFalse(lite.capturesScreenBackdrop)
        assertFalse(lite.capturesBackgroundBackdrop)
        // Without blur the navigation bar uses the stronger lightweight tint.
        assertEquals(lite.chromeSurfaceColor, lite.blurredChromeSurfaceColor)
    }

    @Test
    fun off_rendersNoGlass() {
        val off = style(GlassMode.OFF)
        (standardSurfaces + prominentSurfaces).forEach { assertFalse(off.usesBackdrop(it)) }
        assertFalse(off.isEnabled)
        assertFalse(off.capturesScreenBackdrop)
        assertFalse(off.capturesBackgroundBackdrop)
    }

    @Test
    fun onlyNavigationBarDialogsAndSheetsAreProminent() {
        // The navigation bar plus one overlay at a time: at most two blurred surfaces in Lite.
        standardSurfaces.forEach { assertEquals(GlassEmphasis.STANDARD, it.emphasis) }
        prominentSurfaces.forEach { assertEquals(GlassEmphasis.PROMINENT, it.emphasis) }
    }

    @Test
    fun liteTints_areReadableWithoutBlur_andFollowTheTheme() {
        val lite = style(GlassMode.LITE)
        val full = style(GlassMode.FULL)
        assertTrue(lite.surfaceColor.alpha > full.surfaceColor.alpha)
        assertTrue(lite.surfaceColor.alpha >= 0.58f)
        assertTrue(lite.blurredChromeSurfaceColor.alpha < lite.chromeSurfaceColor.alpha)
        val liteDark = style(GlassMode.LITE, dark = true)
        assertNotEquals(lite.liteRimColors, liteDark.liteRimColors)
        assertNotEquals(lite.surfaceColor, liteDark.surfaceColor)
    }
}
// ===== END SECTION: TEST_GLASS_STYLE =====
