package com.kaushalkr.axiom.core.designsystem.glass

import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.platform.GlassDeviceCapability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// ===== SECTION: TEST_GLASS_MODE_RESOLVER =====
class GlassModeResolverTest {

    private val modernDevice = GlassDeviceCapability.Full
    private val android12Device = modernDevice.copy(supportsRefraction = false, sdkInt = 31)
    private val android11Device = modernDevice.copy(supportsBlur = false, supportsRefraction = false, sdkInt = 30)
    private val android9Device = android11Device.copy(sdkInt = 28)
    private val lowRamDevice = modernDevice.copy(isLowRamDevice = true)
    private val threeGigabytePhone = modernDevice.copy(totalMemoryBytes = 2_800L * 1024 * 1024)
    private val fourGigabytePhone = modernDevice.copy(totalMemoryBytes = 3_700L * 1024 * 1024)

    @Test
    fun modernDevice_rendersRequestedMode() {
        GlassMode.entries.forEach { requested ->
            val result = resolve(requested, modernDevice)
            assertEquals(requested, result.effective)
            assertEquals(GlassLimitation.NONE, result.limitation)
            assertTrue(result.allowsBackdropBlur)
        }
    }

    @Test
    fun withoutRuntimeShader_fullIsCappedToLite() {
        val result = resolve(GlassMode.FULL, android12Device)
        assertEquals(GlassMode.LITE, result.effective)
        assertEquals(GlassLimitation.NO_REFRACTION_SUPPORT, result.limitation)
        assertTrue(result.allowsBackdropBlur)
    }

    @Test
    fun withoutRenderEffect_liteStillRunsWithoutBlur() {
        val full = resolve(GlassMode.FULL, android11Device)
        assertEquals(GlassMode.LITE, full.effective)
        assertEquals(GlassLimitation.NO_BLUR_SUPPORT, full.limitation)
        assertFalse(full.allowsBackdropBlur)

        val lite = resolve(GlassMode.LITE, android11Device)
        assertEquals(GlassMode.LITE, lite.effective)
        assertEquals(GlassLimitation.NONE, lite.limitation)
        assertFalse(lite.allowsBackdropBlur)
    }

    @Test
    fun lowRamDevice_fullIsCappedToLite() {
        val result = resolve(GlassMode.FULL, lowRamDevice)
        assertEquals(GlassMode.LITE, result.effective)
        assertEquals(GlassLimitation.LOW_RAM_DEVICE, result.limitation)
    }

    @Test
    fun batterySaver_capsFullToLite_andDropsBlur_onlyWhenAdaptationEnabled() {
        val adapted = resolve(GlassMode.FULL, modernDevice, powerSave = true, adapt = true)
        assertEquals(GlassMode.LITE, adapted.effective)
        assertEquals(GlassLimitation.BATTERY_SAVER, adapted.limitation)
        assertFalse(adapted.allowsBackdropBlur)

        val liteAdapted = resolve(GlassMode.LITE, modernDevice, powerSave = true, adapt = true)
        assertEquals(GlassMode.LITE, liteAdapted.effective)
        assertFalse(liteAdapted.allowsBackdropBlur)

        val notAdapted = resolve(GlassMode.FULL, modernDevice, powerSave = true, adapt = false)
        assertEquals(GlassMode.FULL, notAdapted.effective)
        assertEquals(GlassLimitation.NONE, notAdapted.limitation)
        assertTrue(notAdapted.allowsBackdropBlur)
    }

    @Test
    fun cheaperRequests_areNeverRaised() {
        val lite = resolve(GlassMode.LITE, modernDevice, powerSave = true, adapt = true)
        assertEquals(GlassMode.LITE, lite.effective)
        assertEquals(GlassLimitation.NONE, lite.limitation)

        val off = resolve(GlassMode.OFF, android12Device)
        assertEquals(GlassMode.OFF, off.effective)
        assertEquals(GlassLimitation.NONE, off.limitation)
    }

    @Test
    fun recommendation_isLite_andOffForWeakDevices() {
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(modernDevice))
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(android12Device))
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(android11Device))
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(fourGigabytePhone))
        assertEquals(GlassMode.OFF, GlassModeResolver.recommend(android9Device))
        assertEquals(GlassMode.OFF, GlassModeResolver.recommend(lowRamDevice))
        assertEquals(GlassMode.OFF, GlassModeResolver.recommend(threeGigabytePhone))
        // Unknown memory (0) is not treated as weak.
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(modernDevice.copy(totalMemoryBytes = 0)))
    }

    @Test
    fun newInstall_followsRecommendation_asLite() {
        val automatic = GlassModeResolver.resolveWithRecommendation(null, modernDevice, isPowerSaveMode = false, adaptToBatterySaver = true)
        assertEquals(GlassMode.LITE, automatic.effective)
        assertEquals(GlassMode.LITE, automatic.recommended)
        assertEquals(true, automatic.followsRecommendation)
        assertEquals(GlassMode.LITE, GlassResolution.Default.effective)
    }

    @Test
    fun manualChoice_overridesRecommendation() {
        val manual = GlassModeResolver.resolveWithRecommendation(GlassMode.FULL, fourGigabytePhone, isPowerSaveMode = false, adaptToBatterySaver = true)
        assertEquals(GlassMode.FULL, manual.effective)
        assertEquals(GlassMode.LITE, manual.recommended)
        assertEquals(false, manual.followsRecommendation)

        val weak = GlassModeResolver.resolveWithRecommendation(GlassMode.LITE, threeGigabytePhone, isPowerSaveMode = false, adaptToBatterySaver = true)
        assertEquals(GlassMode.LITE, weak.effective)
        assertEquals(GlassMode.OFF, weak.recommended)
    }

    private fun resolve(
        requested: GlassMode,
        capability: GlassDeviceCapability,
        powerSave: Boolean = false,
        adapt: Boolean = true,
    ) = GlassModeResolver.resolve(requested, capability, powerSave, adapt)
}
// ===== END SECTION: TEST_GLASS_MODE_RESOLVER =====
