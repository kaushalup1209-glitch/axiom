package com.kaushalkr.axiom.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ===== SECTION: MOTION_PRESS_INDICATION =====
/**
 * App-wide replacement for the Material ripple: a soft overlay that fades in on press and
 * eases out after release. A quick tap still shows a complete, gentle flash because the
 * release waits for the fade-in to finish.
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

    private val progress = Animatable(0f)
    private var pressJob: Job? = null

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        pressJob = launch {
                            progress.animateTo(1f, motion.tween(AxiomMotionTokens.PressOverlayIn))
                        }
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        val pendingPress = pressJob
                        launch {
                            pendingPress?.join()
                            progress.animateTo(0f, motion.tween(AxiomMotionTokens.PressOverlayOut))
                        }
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        val value = progress.value
        if (value > 0f) {
            // The colour's own alpha is the peak strength; [value] modulates it.
            drawRect(overlayColor, alpha = value)
        }
    }
}
// ===== END SECTION: MOTION_PRESS_INDICATION =====
