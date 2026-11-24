package com.kaushalkr.axiom.core.platform

import android.app.ActivityManager
import android.content.Context
import android.os.Build

// ===== SECTION: GLASS_DEVICE_CAPABILITY =====
/**
 * Static rendering capabilities of the current device, read once at startup.
 *
 * - Blur needs RenderEffect (Android 12, API 31).
 * - Lens refraction needs RuntimeShader (Android 13, API 33).
 */
data class GlassDeviceCapability(
    val supportsBlur: Boolean,
    val supportsRefraction: Boolean,
    val isLowRamDevice: Boolean,
    /** Physical memory reported by the system, in bytes. */
    val totalMemoryBytes: Long,
    /** Android API level; older releases usually ship with weaker GPUs. */
    val sdkInt: Int = Build.VERSION_CODES.TIRAMISU,
) {
    companion object {
        fun from(context: Context): GlassDeviceCapability {
            val activityManager = context.getSystemService(ActivityManager::class.java)
            val memoryInfo = ActivityManager.MemoryInfo().also { activityManager?.getMemoryInfo(it) }
            return GlassDeviceCapability(
                supportsBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                supportsRefraction = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                isLowRamDevice = activityManager?.isLowRamDevice == true,
                totalMemoryBytes = memoryInfo.totalMem,
                sdkInt = Build.VERSION.SDK_INT,
            )
        }

        /** Capability of a modern device; used for previews and tests. */
        val Full = GlassDeviceCapability(
            supportsBlur = true,
            supportsRefraction = true,
            isLowRamDevice = false,
            totalMemoryBytes = 8L * 1024 * 1024 * 1024,
            sdkInt = Build.VERSION_CODES.VANILLA_ICE_CREAM,
        )
    }
}
// ===== END SECTION: GLASS_DEVICE_CAPABILITY =====
