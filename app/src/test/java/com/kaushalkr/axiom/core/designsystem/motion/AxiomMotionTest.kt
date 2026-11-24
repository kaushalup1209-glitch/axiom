package com.kaushalkr.axiom.core.designsystem.motion

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import com.kaushalkr.axiom.core.model.AnimationScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ===== SECTION: TEST_AXIOM_MOTION =====
class AxiomMotionTest {

    private val spring = SpringSpecValues(dampingRatio = 0.9f, stiffness = 400f)
    private val tween = TweenSpecValues(durationMillis = 200, delayMillis = 40)

    @Test
    fun normalScale_keepsTokenValues() {
        val motion = AxiomMotion(AnimationScale.NORMAL.factor)
        val springSpec = motion.spring<Float>(spring) as SpringSpec<Float>
        assertEquals(400f, springSpec.stiffness)
        assertEquals(0.9f, springSpec.dampingRatio)
        val tweenSpec = motion.tween<Float>(tween) as TweenSpec<Float>
        assertEquals(200, tweenSpec.durationMillis)
        assertEquals(40, tweenSpec.delay)
    }

    @Test
    fun doubleScale_doublesTweensAndSlowsSpringsWithoutChangingDamping() {
        val motion = AxiomMotion(AnimationScale.DOUBLE.factor)
        val springSpec = motion.spring<Float>(spring) as SpringSpec<Float>
        // Settle time scales with 1/sqrt(stiffness), so 2x duration means stiffness / 4.
        assertEquals(100f, springSpec.stiffness)
        assertEquals(0.9f, springSpec.dampingRatio)
        val tweenSpec = motion.tween<Float>(tween) as TweenSpec<Float>
        assertEquals(400, tweenSpec.durationMillis)
        assertEquals(80, tweenSpec.delay)
    }

    @Test
    fun halfScale_halvesTweensAndStiffensSprings() {
        val motion = AxiomMotion(AnimationScale.HALF.factor)
        assertEquals(1600f, (motion.spring<Float>(spring) as SpringSpec<Float>).stiffness)
        assertEquals(100, (motion.tween<Float>(tween) as TweenSpec<Float>).durationMillis)
    }

    @Test
    fun offScale_snapsEverything() {
        val motion = AxiomMotion(AnimationScale.OFF.factor)
        assertTrue(motion.spring<Float>(spring) is SnapSpec<Float>)
        assertTrue(motion.tween<Float>(tween) is SnapSpec<Float>)
    }
}
// ===== END SECTION: TEST_AXIOM_MOTION =====
