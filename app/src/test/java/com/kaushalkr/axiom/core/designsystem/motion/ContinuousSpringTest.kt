package com.kaushalkr.axiom.core.designsystem.motion

import androidx.compose.runtime.BroadcastFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ===== SECTION: TEST_CONTINUOUS_SPRING =====
@OptIn(ExperimentalCoroutinesApi::class)
class ContinuousSpringTest {

    private val press = SpringSpecValues(dampingRatio = 0.85f, stiffness = 520f)
    private val release = SpringSpecValues(dampingRatio = 0.9f, stiffness = 240f)
    private val clock = BroadcastFrameClock()
    private var frameTime = 0L

    private fun frame() {
        frameTime += FRAME_NANOS
        clock.sendFrame(frameTime)
    }

    private fun springTest(block: (ContinuousSpring) -> Unit) = runTest(UnconfinedTestDispatcher()) {
        val scope = CoroutineScope(coroutineContext + clock + Job())
        try {
            block(ContinuousSpring(0f, scope, AxiomMotion.Default))
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun firstFrameAfterStart_advances() = springTest { spring ->
        spring.animateTo(1f, press)
        frame()
        assertTrue("value should move on the first frame", spring.value > 0f)
    }

    @Test
    fun pressToRelease_keepsMovingWithContinuousVelocity() = springTest { spring ->
        spring.animateTo(1f, press)
        repeat(4) { frame() }
        val valueBefore = spring.value
        val velocityBefore = spring.velocity
        assertTrue(velocityBefore > 0f)

        spring.animateTo(0f, release)
        frame()

        assertNotEquals("no idle frame at the reversal", valueBefore, spring.value)
        // One frame of deceleration cannot flip a strong upward velocity: it carries through.
        assertTrue(spring.velocity > 0f)
        assertTrue(spring.velocity < velocityBefore)
        assertTrue(spring.isRunning)
    }

    @Test
    fun settlesExactlyOnTargetAndStops() = springTest { spring ->
        spring.animateTo(1f, press)
        var frames = 0
        while (spring.isRunning && frames < 1_000) {
            frame()
            frames++
        }
        assertFalse(spring.isRunning)
        assertEquals(1f, spring.value)
        assertEquals(0f, spring.velocity)
    }

    @Test
    fun animationScaleOff_snapsWithoutFrames() = runTest(UnconfinedTestDispatcher()) {
        val scope = CoroutineScope(coroutineContext + clock + Job())
        val spring = ContinuousSpring(0f, scope, AxiomMotion(scale = 0f))
        spring.animateTo(1f, press)
        assertEquals(1f, spring.value)
        assertFalse(spring.isRunning)
        scope.cancel()
    }

    private companion object {
        const val FRAME_NANOS = 8_333_333L
    }
}
// ===== END SECTION: TEST_CONTINUOUS_SPRING =====
