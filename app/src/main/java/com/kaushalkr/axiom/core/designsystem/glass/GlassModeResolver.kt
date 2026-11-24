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
 */
data class GlassResolution(
    val requested: GlassMode,
    val effective: GlassMode,
    val limitation: GlassLimitation,
    val recommended: GlassMode = requested,
    val followsRecommendation: Boolean = true,
) {
    companion object {
        val Default = GlassResolution(GlassMode.FULL, GlassMode.FULL, GlassLimitation.NONE)
    }
}
// ===== END SECTION: GLASS_RESOLUTION =====

// ===== SECTION: GLASS_RECOMMENDATION_TOKENS =====
/** Device thresholds behind the automatic glass recommendation. */
object GlassRecommendationTokens {
    /**
     * Phones below this much memory (3–4 GB models report about 2.8–3.8 GB) are steered to
     * Lite; 6 GB and larger phones report above it and get Full.
     */
    const val FULL_GLASS_MIN_MEMORY_BYTES: Long = 5L * 1024 * 1024 * 1024
}
// ===== END SECTION: GLASS_RECOMMENDATION_TOKENS =====

// ===== SECTION: GLASS_MODE_RESOLVER =====
/**
 * Chooses a recommended glass level for the device, then caps the requested level by device
 * support and power state. Pure functions so they can be unit tested without Android.
 */
object GlassModeResolver {

    /** The glass level suggested for this device when the user has not chosen one. */
    fun recommend(capability: GlassDeviceCapability): GlassMode = when {
        !capability.supportsBlur -> GlassMode.OFF
        !capability.supportsRefraction -> GlassMode.LITE
        capability.isLowRamDevice -> GlassMode.LITE
        capability.totalMemoryBytes in 1 until GlassRecommendationTokens.FULL_GLASS_MIN_MEMORY_BYTES -> GlassMode.LITE
        else -> GlassMode.FULL
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
        var ceiling: GlassMode
        var reason: GlassLimitation
        when {
            !capability.supportsBlur -> {
                ceiling = GlassMode.OFF
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
            else -> {
                ceiling = GlassMode.FULL
                reason = GlassLimitation.NONE
            }
        }
        if (isPowerSaveMode && adaptToBatterySaver && ceiling == GlassMode.FULL) {
            ceiling = GlassMode.LITE
            reason = GlassLimitation.BATTERY_SAVER
        }

        // GlassMode is declared from most to least expensive, so a higher ordinal is cheaper.
        val effective = if (requested.ordinal >= ceiling.ordinal) requested else ceiling
        return GlassResolution(
            requested = requested,
            effective = effective,
            limitation = if (effective == requested) GlassLimitation.NONE else reason,
        )
    }
}
// ===== END SECTION: GLASS_MODE_RESOLVER =====
