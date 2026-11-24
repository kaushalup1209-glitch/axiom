package com.kaushalkr.axiom.core.designsystem.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.roundToInt
import androidx.compose.animation.core.spring as composeSpring
import androidx.compose.animation.core.tween as composeTween

// ===== SECTION: MOTION_SPRING_SPEC =====
/** Unscaled spring parameters. Turned into an animation spec by [AxiomMotion.spring]. */
@Immutable
data class SpringSpecValues(
    val dampingRatio: Float,
    val stiffness: Float,
)

/** Unscaled tween parameters. Turned into an animation spec by [AxiomMotion.tween]. */
@Immutable
data class TweenSpecValues(
    val durationMillis: Int,
    val delayMillis: Int = 0,
    val easing: Easing = AxiomEasing.EmphasizedDecelerate,
)
// ===== END SECTION: MOTION_SPRING_SPEC =====

// ===== SECTION: MOTION_EASING =====
object AxiomEasing {
    /** Fast start, long gentle settle. Used for elements entering. */
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Gentle start, quick finish. Used for elements leaving. */
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Symmetric ease for cross-fades. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
// ===== END SECTION: MOTION_EASING =====

// ===== SECTION: MOTION_TOKENS =====
/**
 * Every animation value in Axiom, defined once at 1x speed. Change motion here, not in
 * components. Springs use damping ratios of 0.8 or more for a smooth settle without visible
 * bounce, except the Liquid Glass drag springs, which keep the Backdrop catalog's values.
 */
object AxiomMotionTokens {
    // Press feedback on buttons and rows
    val PressIn = SpringSpecValues(dampingRatio = 0.85f, stiffness = 520f)
    val PressRelease = SpringSpecValues(dampingRatio = 0.9f, stiffness = 240f)
    val PressFollow = SpringSpecValues(dampingRatio = 0.88f, stiffness = 360f)
    val PressOverlayIn = SpringSpecValues(dampingRatio = 1f, stiffness = 900f)
    val PressOverlayRelease = SpringSpecValues(dampingRatio = 1f, stiffness = 200f)

    /** A quick tap keeps rising to this level before fading, so it stays visible. */
    const val PRESS_OVERLAY_MIN_PEAK = 0.6f

    // Liquid Glass drag (indicator, toggle and slider thumbs) — Backdrop catalog values
    val DragValue = SpringSpecValues(dampingRatio = 1f, stiffness = 1000f)
    val DragVelocity = SpringSpecValues(dampingRatio = 0.5f, stiffness = 300f)
    val DragPress = SpringSpecValues(dampingRatio = 1f, stiffness = 1000f)
    val DragScaleX = SpringSpecValues(dampingRatio = 0.6f, stiffness = 250f)
    val DragScaleY = SpringSpecValues(dampingRatio = 0.7f, stiffness = 250f)
    val DragPanelReturn = SpringSpecValues(dampingRatio = 1f, stiffness = 300f)

    // Tab-bar press light — Backdrop catalog values, kept so the tab swipe feels unchanged
    val CatalogHighlightPress = SpringSpecValues(dampingRatio = 0.5f, stiffness = 300f)
    val CatalogHighlightFollow = SpringSpecValues(dampingRatio = 0.5f, stiffness = 300f)

    // Screen transitions
    val TabFadeIn = TweenSpecValues(durationMillis = 260, delayMillis = 40, easing = AxiomEasing.EmphasizedDecelerate)
    val TabFadeOut = TweenSpecValues(durationMillis = 140, easing = AxiomEasing.EmphasizedAccelerate)
    const val TAB_ENTER_SCALE = 0.985f
    val PushSlide = SpringSpecValues(dampingRatio = 1f, stiffness = 420f)
    val PushFadeIn = TweenSpecValues(durationMillis = 300, easing = AxiomEasing.EmphasizedDecelerate)
    val PushFadeOut = TweenSpecValues(durationMillis = 220, easing = AxiomEasing.EmphasizedAccelerate)
    const val PUSH_UNDERLAY_SCALE = 0.96f
    const val PUSH_OFFSET_FRACTION = 0.28f

    // Floating chrome (navigation bar, top buttons)
    val ChromeShow = SpringSpecValues(dampingRatio = 0.92f, stiffness = 380f)
    val ChromeHide = TweenSpecValues(durationMillis = 220, easing = AxiomEasing.EmphasizedAccelerate)
    val ChromeFadeIn = TweenSpecValues(durationMillis = 220, easing = AxiomEasing.Standard)
    val ChromeFadeOut = TweenSpecValues(durationMillis = 160, easing = AxiomEasing.Standard)
    const val CHROME_HIDDEN_SCALE = 0.85f

    // Dialogs
    val ScrimIn = TweenSpecValues(durationMillis = 220, easing = AxiomEasing.Standard)
    val ScrimOut = TweenSpecValues(durationMillis = 180, easing = AxiomEasing.Standard)
    val DialogIn = SpringSpecValues(dampingRatio = 0.86f, stiffness = 420f)
    val DialogOut = TweenSpecValues(durationMillis = 160, easing = AxiomEasing.EmphasizedAccelerate)
    const val DIALOG_HIDDEN_SCALE = 0.92f

    // Expanding and collapsing content
    val Expand = SpringSpecValues(dampingRatio = 0.95f, stiffness = 380f)
    val ExpandFade = TweenSpecValues(durationMillis = 220, easing = AxiomEasing.Standard)

    // Progress rings and bars filling to a new value (critically damped, no overshoot)
    val ProgressFill = SpringSpecValues(dampingRatio = 1f, stiffness = 90f)

    // Lists: items moving, appearing and disappearing
    val ListPlacement = SpringSpecValues(dampingRatio = 0.9f, stiffness = 420f)
    val ListFadeIn = TweenSpecValues(durationMillis = 220, easing = AxiomEasing.Standard)
    val ListFadeOut = TweenSpecValues(durationMillis = 160, easing = AxiomEasing.Standard)
    /** A dragged list item lifting off and settling back. */
    val DragLift = SpringSpecValues(dampingRatio = 0.85f, stiffness = 600f)
    const val DRAG_LIFT_SCALE = 1.03f

    // Bottom sheets
    val SheetIn = SpringSpecValues(dampingRatio = 0.9f, stiffness = 380f)
    val SheetOut = TweenSpecValues(durationMillis = 200, easing = AxiomEasing.EmphasizedAccelerate)

    // Undo snackbar
    val SnackbarIn = SpringSpecValues(dampingRatio = 0.88f, stiffness = 460f)
    val SnackbarOut = TweenSpecValues(durationMillis = 160, easing = AxiomEasing.EmphasizedAccelerate)
    const val SNACKBAR_DURATION_MILLIS = 5_000L

    // Swapping one block of content for another (e.g. timer controls)
    val ContentSwapIn = TweenSpecValues(durationMillis = 220, delayMillis = 60, easing = AxiomEasing.EmphasizedDecelerate)
    val ContentSwapOut = TweenSpecValues(durationMillis = 120, easing = AxiomEasing.EmphasizedAccelerate)
    val ContentSwapSize = SpringSpecValues(dampingRatio = 1f, stiffness = 420f)
}
// ===== END SECTION: MOTION_TOKENS =====

// ===== SECTION: MOTION_SCHEME =====
/**
 * Converts [AxiomMotionTokens] into animation specs at the user's animation scale.
 *
 * - Tweens multiply duration and delay by [scale].
 * - Springs divide stiffness by scale², which multiplies their settle time by [scale]
 *   while keeping the same damping (shape) of the motion.
 * - A scale of 0 returns [snap], so values jump straight to their target.
 *
 * Android's own animator duration scale still applies on top of this.
 */
@Immutable
data class AxiomMotion(val scale: Float) {

    val isEnabled: Boolean get() = scale > 0f

    fun <T> spring(values: SpringSpecValues, visibilityThreshold: T? = null): FiniteAnimationSpec<T> =
        if (!isEnabled) {
            snap()
        } else {
            composeSpring(
                dampingRatio = values.dampingRatio,
                stiffness = values.stiffness / (scale * scale),
                visibilityThreshold = visibilityThreshold,
            )
        }

    fun <T> tween(values: TweenSpecValues): FiniteAnimationSpec<T> =
        if (!isEnabled) {
            snap()
        } else {
            composeTween(
                durationMillis = (values.durationMillis * scale).roundToInt(),
                delayMillis = (values.delayMillis * scale).roundToInt(),
                easing = values.easing,
            )
        }

    companion object {
        val Default = AxiomMotion(scale = 1f)
    }
}

val LocalAxiomMotion = staticCompositionLocalOf { AxiomMotion.Default }
// ===== END SECTION: MOTION_SCHEME =====
