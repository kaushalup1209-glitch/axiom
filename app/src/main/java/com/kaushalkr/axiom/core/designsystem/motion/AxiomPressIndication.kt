package com.kaushalkr.axiom.core.designsystem.motion

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ===== SECTION: MOTION_PRESS_INDICATION =====
/**
 * App-wide replacement for the Material ripple: a soft overlay that rises on press and
 * eases out after release. One [ContinuousSpring] is retargeted for press and release, so
 * the overlay never pauses or restarts at the reversal. A quick tap keeps rising to
 * [AxiomMotionTokens.PRESS_OVERLAY_MIN_PEAK] (without stopping) before it fades.
 */
class AxiomPressIndication(
    private val overlayColor: Color,
    private val motion: AxiomMotion,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        PressOverlayNode(interactionSource, overlayColor, motion)

    override fun equals(other: Any?): Boolean =
        other is AxiomPressIndication && other.overlayColor == overlayColor && other.motion == motion

    override fun hashCode(): Int = 31 * overlayColor.hashCode() + motion.hashCode()
}

private class PressOverlayNode(
    private val interactionSource: InteractionSource,
    private val overlayColor: Color,
    private val motion: AxiomMotion,
) : Modifier.Node(), DrawModifierNode {

    private var progress: ContinuousSpring? = null
    private var releaseJob: Job? = null

    override fun onAttach() {
        val spring = ContinuousSpring(0f, coroutineScope, motion).also { progress = it }
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        releaseJob?.cancel()
                        spring.animateTo(1f, AxiomMotionTokens.PressOverlayIn)
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        releaseJob?.cancel()
                        releaseJob = coroutineScope.launch {
                            while (spring.targetValue == 1f && spring.value < AxiomMotionTokens.PRESS_OVERLAY_MIN_PEAK) {
                                withFrameNanos { }
                            }
                            spring.animateTo(0f, AxiomMotionTokens.PressOverlayRelease)
                        }
                    }
                }
            }
        }
    }

    override fun onDetach() {
        progress = null
        releaseJob = null
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val value = progress?.value ?: 0f
        if (value > 0f) {
            // The colour's own alpha is the peak strength; [value] modulates it.
            drawRect(overlayColor, alpha = value.coerceAtMost(1f))
        }
    }
}
// ===== END SECTION: MOTION_PRESS_INDICATION =====
