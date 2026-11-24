package com.kaushalkr.axiom.core.designsystem.glass.shader

import android.os.Build
import androidx.annotation.RequiresApi
import com.kyant.backdrop.RuntimeShader
import java.util.concurrent.ConcurrentHashMap

// ===== SECTION: GLASS_SHADER_CACHE =====
/**
 * Process-wide cache of compiled Liquid Glass shaders.
 *
 * Constructing a RuntimeShader compiles its AGSL synchronously. The Backdrop library keeps
 * one shader per glass element, so a screen with many glass surfaces compiled dozens of
 * shaders on the main thread the frame it appeared, which showed up as a hitch right after
 * a button press that navigates. Here each source is compiled once, ideally off the main
 * thread by [prewarm], and shared.
 *
 * Sharing is safe because shaders are only configured on the main thread, and Android
 * snapshots a shader with its current uniforms when a draw is recorded or a RenderEffect
 * is created.
 */
object GlassShaderCache {

    enum class Kind(internal val source: String) {
        REFRACTION(RefractionShaderSource),
        REFRACTION_WITH_DISPERSION(RefractionWithDispersionShaderSource),
        DEFAULT_HIGHLIGHT(DefaultHighlightShaderSource),
        AMBIENT_HIGHLIGHT(AmbientHighlightShaderSource),
        INTERACTIVE_HIGHLIGHT(InteractiveHighlightShaderSource),
    }

    private val shaders = ConcurrentHashMap<Kind, RuntimeShader>()

    /** The shared shader for [kind]; compiles it on first use. */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun obtain(kind: Kind): RuntimeShader =
        shaders.computeIfAbsent(kind) { RuntimeShader(it.source) }

    /** Compiles every glass shader ahead of first use. Safe to call from any thread. */
    fun prewarm() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        Kind.entries.forEach { obtain(it) }
    }
}
// ===== END SECTION: GLASS_SHADER_CACHE =====
