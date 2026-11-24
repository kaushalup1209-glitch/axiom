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
/** The user's requested glass level, the level actually rendered, and the reason for any difference. */
data class GlassResolution(
    val requested: GlassMode,
    val effective: GlassMode,
    val limitation: GlassLimitation,
) {
    companion object {
        val Default = GlassResolution(GlassMode.FULL, GlassMode.FULL, GlassLimitation.NONE)
    }
}
// ===== END SECTION: GLASS_RESOLUTION =====

// ===== SECTION: GLASS_MODE_RESOLVER =====
/**
 * Caps the requested glass level by device support and power state.
 * Pure function so it can be unit tested without Android.
 */
object GlassModeResolver {

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
