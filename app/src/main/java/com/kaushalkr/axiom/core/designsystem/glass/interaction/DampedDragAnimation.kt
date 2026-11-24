/*
 * Copyright 2025 Kyant
 * Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 *
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/utils/DampedDragAnimation.kt
 * Changes for Axiom: package name, Android SystemClock as the velocity time source,
 * direct use of kotlinx.coroutines.android.awaitFrame, spring values moved to
 * AxiomMotionTokens (same values) and scaled by the user's animation scale, press and
 * stretch driven by ContinuousSpring so press→release reverses without an idle frame,
 * a guard so a stale release cannot cancel a newer press, and section labels.
 */
package com.kaushalkr.axiom.core.designsystem.glass.interaction

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotion
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.ContinuousSpring
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

// ===== SECTION: GLASS_DAMPED_DRAG_ANIMATION =====
/**
 * Spring-driven value with press, stretch and velocity state. Powers the fluid
 * Liquid Glass thumbs and indicators (toggle, slider and tab bar).
 */
class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
    val motion: AxiomMotion = AxiomMotion.Default,
) {

    // ===== SECTION: GLASS_DAMPED_DRAG_SPECS =====
    private val valueAnimationSpec =
        motion.spring(AxiomMotionTokens.DragValue, visibilityThreshold)
    private val velocityAnimationSpec =
        motion.spring(AxiomMotionTokens.DragVelocity, visibilityThreshold * 10f)
    // ===== END SECTION: GLASS_DAMPED_DRAG_SPECS =====

    private val valueAnimation =
        Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation =
        Animatable(0f, 5f)
    // Press and stretch are retargeted, never restarted (see ContinuousSpring).
    private val pressProgressSpring = ContinuousSpring(0f, animationScope, motion)
    private val scaleXSpring = ContinuousSpring(initialScale, animationScope, motion)
    private val scaleYSpring = ContinuousSpring(initialScale, animationScope, motion)
    private var pressGeneration = 0

    private val mutatorMutex = MutatorMutex()

    private val velocityTracker = VelocityTracker()

    val value: Float get() = valueAnimation.value
    val progress: Float get() = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val targetValue: Float get() = valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressSpring.value
    val scaleX: Float get() = scaleXSpring.value
    val scaleY: Float get() = scaleYSpring.value
    val velocity: Float get() = velocityAnimation.value

    // ===== SECTION: GLASS_DAMPED_DRAG_GESTURE_MODIFIER =====
    val modifier: Modifier = Modifier.pointerInput(Unit) {
        inspectDragGestures(
            onDragStart = { down ->
                onDragStarted(down.position)
                press()
            },
            onDragEnd = {
                onDragStopped()
                release()
            },
            onDragCancel = {
                onDragStopped()
                release()
            },
        ) { _, dragAmount ->
            onDrag(size, dragAmount)
        }
    }
    // ===== END SECTION: GLASS_DAMPED_DRAG_GESTURE_MODIFIER =====

    // ===== SECTION: GLASS_DAMPED_DRAG_CONTROL =====
    fun press() {
        velocityTracker.resetTracking()
        pressGeneration++
        pressProgressSpring.animateTo(1f, AxiomMotionTokens.DragPress)
        scaleXSpring.animateTo(pressedScale, AxiomMotionTokens.DragScaleX)
        scaleYSpring.animateTo(pressedScale, AxiomMotionTokens.DragScaleY)
    }

    fun release() {
        val releasingGeneration = pressGeneration
        animationScope.launch {
            awaitFrame()
            if (value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - valueAnimation.targetValue) < threshold }
                    .first()
            }
            // A newer press owns the springs; leave them pressed.
            if (releasingGeneration != pressGeneration) return@launch
            pressProgressSpring.animateTo(0f, AxiomMotionTokens.DragPress)
            scaleXSpring.animateTo(initialScale, AxiomMotionTokens.DragScaleX)
            scaleYSpring.animateTo(initialScale, AxiomMotionTokens.DragScaleY)
        }
    }

    fun updateValue(value: Float) {
        val targetValue = value.coerceIn(valueRange)
        animationScope.launch {
            launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) { updateVelocity() } }
        }
    }

    fun animateToValue(value: Float) {
        animationScope.launch {
            mutatorMutex.mutate {
                press()
                val targetValue = value.coerceIn(valueRange)
                launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) }
                if (velocity != 0f) {
                    launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
                }
                release()
            }
        }
    }

    private fun updateVelocity() {
        velocityTracker.addPosition(
            SystemClock.uptimeMillis(),
            Offset(value, 0f),
        )
        val targetVelocity = velocityTracker.calculateVelocity().x / (valueRange.endInclusive - valueRange.start)
        animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
    }
    // ===== END SECTION: GLASS_DAMPED_DRAG_CONTROL =====
}
// ===== END SECTION: GLASS_DAMPED_DRAG_ANIMATION =====
