package com.kaushalkr.axiom.core.designsystem.motion

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.sqrt

// ===== SECTION: CONTINUOUS_SPRING =====
/**
 * A frame-driven spring that is retargeted instead of restarted.
 *
 * `Animatable.animateTo` starts a new animation on every call, and each new animation spends
 * its first frame at play time zero, so the value holds still for one frame. Calling it on
 * every press, release or pointer move produces exactly the "hesitate, then continue"
 * micro-stutter. This spring keeps one running loop: a new target only changes where the
 * spring pulls, so position and velocity carry through every press→release reversal.
 *
 * - Advances on the very first frame after a retarget (no idle frame).
 * - Integrates with fixed sub-steps, so 60, 90 and 120 Hz displays produce the same motion.
 * - Honours [AxiomMotion] (stiffness ÷ scale², snap when off) and Android's animator scale.
 * - Stops its frame loop once settled, so it costs nothing at rest.
 *
 * Must be used from the main thread. [value] is snapshot state; read it in layout, draw or
 * graphics-layer blocks to avoid recomposition.
 */
@Stable
class ContinuousSpring(
    initialValue: Float,
    private val scope: CoroutineScope,
    private val motion: AxiomMotion,
    private val visibilityThreshold: Float = 0.001f,
) {
    private val valueState = mutableFloatStateOf(initialValue)

    val value: Float get() = valueState.floatValue

    var velocity: Float = 0f
        private set

    var targetValue: Float = initialValue
        private set

    val isRunning: Boolean get() = job?.isActive == true

    private var stiffness = 0f
    private var dampingRatio = 1f
    private var job: Job? = null
    private var lastFrameNanos = UNSET_TIME
    private var retargetNanos = 0L

    // ===== SECTION: CONTINUOUS_SPRING_CONTROL =====
    /** Pulls toward [target] with [spring]; keeps the current value and velocity. */
    fun animateTo(target: Float, spring: SpringSpecValues) {
        targetValue = target
        if (!motion.isEnabled) {
            snapTo(target)
            return
        }
        stiffness = spring.stiffness / (motion.scale * motion.scale)
        dampingRatio = spring.dampingRatio
        if (isRunning) return
        retargetNanos = System.nanoTime()
        lastFrameNanos = UNSET_TIME
        job = scope.launch { runFrames() }
    }

    /** Jumps to [target] and stops. */
    fun snapTo(target: Float) {
        job?.cancel()
        job = null
        targetValue = target
        velocity = 0f
        valueState.floatValue = target
    }
    // ===== END SECTION: CONTINUOUS_SPRING_CONTROL =====

    // ===== SECTION: CONTINUOUS_SPRING_INTEGRATION =====
    private suspend fun runFrames() {
        val systemScale = coroutineContext[MotionDurationScale]
        while (true) {
            val settled = withFrameNanos { frameNanos ->
                val scaleFactor = systemScale?.scaleFactor ?: 1f
                if (scaleFactor <= 0f) {
                    settle()
                    true
                } else {
                    step(frameNanos, scaleFactor)
                }
            }
            if (settled) break
        }
    }

    private fun step(frameNanos: Long, systemScale: Float): Boolean {
        val previousNanos = if (lastFrameNanos == UNSET_TIME) retargetNanos else lastFrameNanos
        val elapsedNanos = frameNanos - previousNanos
        lastFrameNanos = frameNanos
        // The first frame after a retarget always advances, even if its timestamp predates the call.
        val frameSeconds = if (elapsedNanos in 1..MAX_FRAME_NANOS) {
            elapsedNanos / NANOS_PER_SECOND
        } else {
            NOMINAL_FRAME_SECONDS
        }
        var remaining = frameSeconds / systemScale

        val omega = sqrt(stiffness)
        val damping = 2f * dampingRatio * omega
        var position = valueState.floatValue
        var currentVelocity = velocity
        while (remaining > 0f) {
            val dt = minOf(remaining, MAX_SUB_STEP_SECONDS)
            // Semi-implicit Euler: stable for these stiffness values at a 2 ms sub-step.
            val acceleration = -stiffness * (position - targetValue) - damping * currentVelocity
            currentVelocity += acceleration * dt
            position += currentVelocity * dt
            remaining -= dt
        }
        velocity = currentVelocity

        val settled = abs(position - targetValue) < visibilityThreshold &&
            abs(currentVelocity) < visibilityThreshold * VELOCITY_THRESHOLD_FACTOR
        if (settled) {
            settle()
        } else {
            valueState.floatValue = position
        }
        return settled
    }

    private fun settle() {
        velocity = 0f
        valueState.floatValue = targetValue
        lastFrameNanos = UNSET_TIME
    }
    // ===== END SECTION: CONTINUOUS_SPRING_INTEGRATION =====

    private companion object {
        const val UNSET_TIME = Long.MIN_VALUE
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val MAX_FRAME_NANOS = 50_000_000L
        const val NOMINAL_FRAME_SECONDS = 1f / 120f
        const val MAX_SUB_STEP_SECONDS = 0.002f
        const val VELOCITY_THRESHOLD_FACTOR = 60f
    }
}
// ===== END SECTION: CONTINUOUS_SPRING =====
