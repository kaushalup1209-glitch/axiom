package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion

// ===== SECTION: GLASS_OVERLAY_HOST_STATE =====
/**
 * Holds the overlay (dialog) currently shown above the whole app.
 *
 * Overlays must render outside the captured screen so their glass can refract the screen
 * without a capture loop. Screens keep their own saveable visibility flags and register
 * content here through components such as [GlassDialog].
 */
@Stable
class GlassOverlayHostState {
    internal var content: (@Composable () -> Unit)? by mutableStateOf(null)
        private set
    private var owner: Any? = null

    /** True while an overlay is visible; the app hides underlying content from accessibility. */
    val isShowing: Boolean get() = content != null

    internal fun show(owner: Any, content: @Composable () -> Unit) {
        this.owner = owner
        this.content = content
    }

    internal fun hide(owner: Any) {
        if (this.owner === owner) {
            this.owner = null
            this.content = null
        }
    }
}

val LocalGlassOverlayHost = staticCompositionLocalOf<GlassOverlayHostState?> { null }

/** Lets overlay content join the host's enter/exit transition (used for the dialog scale-in). */
internal val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }
// ===== END SECTION: GLASS_OVERLAY_HOST_STATE =====

// ===== SECTION: GLASS_OVERLAY_HOST =====
/** Renders the registered overlay with a fade, keeping the last content during the exit animation. */
@Composable
fun GlassOverlayHost(
    state: GlassOverlayHostState,
    modifier: Modifier = Modifier,
) {
    val motion = LocalAxiomMotion.current
    val current = state.content
    val lastContent = remember { arrayOfNulls<@Composable () -> Unit>(1) }
    if (current != null) lastContent[0] = current

    AnimatedVisibility(
        visible = current != null,
        modifier = modifier,
        enter = fadeIn(motion.tween(AxiomMotionTokens.ScrimIn)),
        exit = fadeOut(motion.tween(AxiomMotionTokens.ScrimOut)),
    ) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            Box(Modifier.fillMaxSize()) {
                (current ?: lastContent[0])?.invoke()
            }
        }
    }
}
// ===== END SECTION: GLASS_OVERLAY_HOST =====
