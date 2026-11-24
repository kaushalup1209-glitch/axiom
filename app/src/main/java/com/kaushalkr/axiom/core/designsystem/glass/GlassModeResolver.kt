package com.kaushalkr.axiom.core.designsystem.glass

import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.platform.GlassDeviceCapability

// ===== SECTION: GLASS_LIMITATION =====
/** Why the rendered glass level is lower than the level the user selected. */
enum class GlassLimitation {
    NONE,
    BATTERY_SAVER,
    NO_REFRACTION_SUPPORT,
    NO_BLUR_SUPPORT,
    LOW_RAM_DEVICE,
}
// ===== END SECTION: GLASS_LIMITATION =====

// ===== SECTION: GLASS_RESOLUTION =====
/**
 * The glass level the user asked for (manually or by following the recommendation), the level
 * actually rendered, the reason for any difference, and the device recommendation.
 *
 * @param allowsBackdropBlur whether Lite may blur the screen behind its prominent surfaces
 *   (navigation bar, dialogs, sheets). False without RenderEffect or while Battery Saver
 *   adaptation is active; Lite then draws every surface as lightweight glass.
 */
data class GlassResolution(
    val requested: GlassMode,
    val effective: GlassMode,
    val limitation: GlassLimitation,
    val recommended: GlassMode = requested,
    val followsRecommendation: Boolean = true,
    val allowsBackdropBlur: Boolean = true,
) {
    companion object {
        val Default = GlassResolution(GlassMode.LITE, GlassMode.LITE, GlassLimitation.NONE)
    }
}
// ===== END SECTION: GLASS_RESOLUTION =====

// ===== SECTION: GLASS_RECOMMENDATION_TOKENS =====
/** Device thresholds behind the automatic glass recommendation. */
object GlassRecommendationTokens {
    /**
     * Phones below this much memory (2 and 3 GB models report about 1.8–2.9 GB) are steered to
     * Off; 4 GB and larger phones report above it and get Lite.
     */
    const val LITE_GLASS_MIN_MEMORY_BYTES: Long = 3_200L * 1024 * 1024

    /** Android 9 and older devices are steered to Off. */
    const val LITE_GLASS_MIN_SDK: Int = 29
}
// ===== END SECTION: GLASS_RECOMMENDATION_TOKENS =====

// ===== SECTION: GLASS_MODE_RESOLVER =====
/**
 * Chooses a recommended glass level for the device, then caps the requested level by device
 * support and power state. Pure functions so they can be unit tested without Android.
 *
 * Lite is the default everywhere it runs well; Full (the original Kyant0 Liquid Glass) is
 * always an explicit choice, and weak devices are steered to Off.
 */
object GlassModeResolver {

    /** The glass level suggested for this device when the user has not chosen one. */
    fun recommend(capability: GlassDeviceCapability): GlassMode = when {
        capability.isLowRamDevice -> GlassMode.OFF
        capability.totalMemoryBytes in 1 until GlassRecommendationTokens.LITE_GLASS_MIN_MEMORY_BYTES -> GlassMode.OFF
        capability.sdkInt < GlassRecommendationTokens.LITE_GLASS_MIN_SDK -> GlassMode.OFF
        else -> GlassMode.LITE
    }

    /**
     * @param manualChoice the user's explicit choice, or null to follow [recommend].
     */
    fun resolveWithRecommendation(
        manualChoice: GlassMode?,
        capability: GlassDeviceCapability,
        isPowerSaveMode: Boolean,
        adaptToBatterySaver: Boolean,
    ): GlassResolution {
        val recommended = recommend(capability)
        return resolve(manualChoice ?: recommended, capability, isPowerSaveMode, adaptToBatterySaver)
            .copy(recommended = recommended, followsRecommendation = manualChoice == null)
    }

    fun resolve(
        requested: GlassMode,
        capability: GlassDeviceCapability,
        isPowerSaveMode: Boolean,
        adaptToBatterySaver: Boolean,
    ): GlassResolution {
        val saveBattery = isPowerSaveMode && adaptToBatterySaver
        // Lite needs nothing beyond plain drawing, so it is the ceiling whenever Full cannot run.
        var ceiling: GlassMode
        var reason: GlassLimitation
        when {
            !capability.supportsBlur -> {
                ceiling = GlassMode.LITE
                reason = GlassLimitation.NO_BLUR_SUPPORT
            }
            !capability.supportsRefraction -> {
                ceiling = GlassMode.LITE
                reason = GlassLimitation.NO_REFRACTION_SUPPORT
            }
            capability.isLowRamDevice -> {
                ceiling = GlassMode.LITE
                reason = GlassLimitation.LOW_RAM_DEVICE
            }
            saveBattery -> {
                ceiling = GlassMode.LITE
                reason = GlassLimitation.BATTERY_SAVER
            }
            else -> {
                ceiling = GlassMode.FULL
                reason = GlassLimitation.NONE
            }
        }

        // GlassMode is declared from most to least expensive, so a higher ordinal is cheaper.
        val effective = if (requested.ordinal >= ceiling.ordinal) requested else ceiling
        return GlassResolution(
            requested = requested,
            effective = effective,
            limitation = if (effective == requested) GlassLimitation.NONE else reason,
            allowsBackdropBlur = capability.supportsBlur && !saveBattery,
        )
    }
}
// ===== END SECTION: GLASS_MODE_RESOLVER =====
