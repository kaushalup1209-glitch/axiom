package com.kaushalkr.axiom.core.designsystem.glass

import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.platform.GlassDeviceCapability
import org.junit.Assert.assertEquals
import org.junit.Test

// ===== SECTION: TEST_GLASS_MODE_RESOLVER =====
class GlassModeResolverTest {

    private val modernDevice = GlassDeviceCapability.Full
    private val android12Device = modernDevice.copy(supportsRefraction = false)
    private val android11Device = modernDevice.copy(supportsBlur = false, supportsRefraction = false)
    private val lowRamDevice = modernDevice.copy(isLowRamDevice = true)

    @Test
    fun modernDevice_rendersRequestedMode() {
        GlassMode.entries.forEach { requested ->
            val result = resolve(requested, modernDevice)
            assertEquals(requested, result.effective)
            assertEquals(GlassLimitation.NONE, result.limitation)
        }
    }

    @Test
    fun withoutRuntimeShader_fullIsCappedToLite() {
        val result = resolve(GlassMode.FULL, android12Device)
        assertEquals(GlassMode.LITE, result.effective)
        assertEquals(GlassLimitation.NO_REFRACTION_SUPPORT, result.limitation)
    }

    @Test
    fun withoutRenderEffect_everythingFallsBackToOff() {
        listOf(GlassMode.FULL, GlassMode.LITE).forEach { requested ->
            val result = resolve(requested, android11Device)
            assertEquals(GlassMode.OFF, result.effective)
            assertEquals(GlassLimitation.NO_BLUR_SUPPORT, result.limitation)
        }
    }

    @Test
    fun lowRamDevice_fullIsCappedToLite() {
        val result = resolve(GlassMode.FULL, lowRamDevice)
        assertEquals(GlassMode.LITE, result.effective)
        assertEquals(GlassLimitation.LOW_RAM_DEVICE, result.limitation)
    }

    @Test
    fun batterySaver_capsFullToLite_onlyWhenAdaptationEnabled() {
        val adapted = resolve(GlassMode.FULL, modernDevice, powerSave = true, adapt = true)
        assertEquals(GlassMode.LITE, adapted.effective)
        assertEquals(GlassLimitation.BATTERY_SAVER, adapted.limitation)

        val notAdapted = resolve(GlassMode.FULL, modernDevice, powerSave = true, adapt = false)
        assertEquals(GlassMode.FULL, notAdapted.effective)
        assertEquals(GlassLimitation.NONE, notAdapted.limitation)
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
    fun recommendation_followsDeviceTier() {
        assertEquals(GlassMode.FULL, GlassModeResolver.recommend(modernDevice))
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(android12Device))
        assertEquals(GlassMode.OFF, GlassModeResolver.recommend(android11Device))
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(lowRamDevice))
        val fourGigabytePhone = modernDevice.copy(totalMemoryBytes = 3_700L * 1024 * 1024)
        assertEquals(GlassMode.LITE, GlassModeResolver.recommend(fourGigabytePhone))
        val sixGigabytePhone = modernDevice.copy(totalMemoryBytes = 5_600L * 1024 * 1024)
        assertEquals(GlassMode.FULL, GlassModeResolver.recommend(sixGigabytePhone))
    }

    @Test
    fun manualChoice_overridesRecommendation() {
        val fourGigabytePhone = modernDevice.copy(totalMemoryBytes = 3_700L * 1024 * 1024)
        val automatic = GlassModeResolver.resolveWithRecommendation(null, fourGigabytePhone, isPowerSaveMode = false, adaptToBatterySaver = true)
        assertEquals(GlassMode.LITE, automatic.effective)
        assertEquals(true, automatic.followsRecommendation)

        val manual = GlassModeResolver.resolveWithRecommendation(GlassMode.FULL, fourGigabytePhone, isPowerSaveMode = false, adaptToBatterySaver = true)
        assertEquals(GlassMode.FULL, manual.effective)
        assertEquals(GlassMode.LITE, manual.recommended)
        assertEquals(false, manual.followsRecommendation)
    }

    private fun resolve(
        requested: GlassMode,
        capability: GlassDeviceCapability,
        powerSave: Boolean = false,
        adapt: Boolean = true,
    ) = GlassModeResolver.resolve(requested, capability, powerSave, adapt)
}
// ===== END SECTION: TEST_GLASS_MODE_RESOLVER =====
