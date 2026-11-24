package com.kaushalkr.axiom.core.designsystem.glass

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop

// ===== SECTION: GLASS_LOCAL_BACKDROP =====
/**
 * The backdrop that glass components draw (refract and blur) behind themselves.
 *
 * The app root provides the ambient background for in-screen glass, and the captured screen
 * for floating chrome. A [com.kaushalkr.axiom.core.designsystem.components.GlassCard]
 * re-provides its own exported backdrop so nested controls render glass-on-glass without
 * a capture loop.
 */
val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop> { emptyBackdrop() }
// ===== END SECTION: GLASS_LOCAL_BACKDROP =====

// ===== SECTION: GLASS_BACKDROP_SOURCE_MODIFIER =====
/**
 * Captures this node's drawing into [backdrop] only when glass is enabled.
 * Skipping the capture in [com.kaushalkr.axiom.core.model.GlassMode.OFF] avoids recording
 * an offscreen layer that nothing would read.
 */
fun Modifier.glassBackdropSource(backdrop: LayerBackdrop, enabled: Boolean): Modifier =
    if (enabled) this.layerBackdrop(backdrop) else this

/** Returns [backdrop] when glass is enabled, otherwise an empty backdrop that draws nothing. */
fun backdropOrEmpty(backdrop: Backdrop, enabled: Boolean): Backdrop =
    if (enabled) backdrop else emptyBackdrop()
// ===== END SECTION: GLASS_BACKDROP_SOURCE_MODIFIER =====
