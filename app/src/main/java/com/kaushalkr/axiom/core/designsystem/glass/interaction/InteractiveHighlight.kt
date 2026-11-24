/*
 * Copyright 2025 Kyant
 * Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 *
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/utils/InteractiveHighlight.kt
 * Changes for Axiom: package name, a [useShader] switch so Lite and Off skip the
 * RuntimeShader light, one shared pre-compiled shader (GlassShaderCache),
 * press and follow motion driven by ContinuousSpring (retargeted, never restarted, so
 * press→release keeps its velocity with no idle frame), configurable springs scaled by
 * the user's animation scale, a configurable flat glow, and section labels.
 * The shader source and drawing are unchanged.
 */
package com.kaushalkr.axiom.core.designsystem.glass.interaction

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastCoerceIn
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotion
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.ContinuousSpring
import com.kaushalkr.axiom.core.designsystem.motion.SpringSpecValues
import com.kaushalkr.axiom.core.designsystem.glass.shader.GlassShaderCache
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.isRuntimeShaderSupported
import kotlinx.coroutines.CoroutineScope

// ===== SECTION: GLASS_INTERACTIVE_HIGHLIGHT =====
/**
 * Press-and-follow light for glass surfaces. With [useShader] the light is a radial
 * RuntimeShader that tracks the finger; otherwise a flat additive glow is used.
 * [pressProgress] and [offset] also drive the press deformation of glass buttons.
 */
class InteractiveHighlight(
    val animationScope: CoroutineScope,
    val useShader: Boolean = true,
    val motion: AxiomMotion = AxiomMotion.Default,
    private val pressSpring: SpringSpecValues = AxiomMotionTokens.PressIn,
    private val releaseSpring: SpringSpecValues = AxiomMotionTokens.PressRelease,
    private val followSpring: SpringSpecValues = AxiomMotionTokens.PressFollow,
    private val flatGlowAlpha: Float = 0.25f,
    val position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset },
) {

    // ===== SECTION: GLASS_INTERACTIVE_HIGHLIGHT_SPRINGS =====
    private val pressProgressSpring = ContinuousSpring(0f, animationScope, motion)
    private val positionXSpring = ContinuousSpring(0f, animationScope, motion, POSITION_THRESHOLD)
    private val positionYSpring = ContinuousSpring(0f, animationScope, motion, POSITION_THRESHOLD)
    // ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT_SPRINGS =====

    private var startPosition = Offset.Zero
    val pressProgress: Float get() = pressProgressSpring.value
    private val animatedPosition: Offset get() = Offset(positionXSpring.value, positionYSpring.value)
    val offset: Offset get() = animatedPosition - startPosition

    private val shader =
        if (useShader && isRuntimeShaderSupported()) GlassShaderCache.obtain(GlassShaderCache.Kind.INTERACTIVE_HIGHLIGHT) else null

    // ===== SECTION: GLASS_INTERACTIVE_HIGHLIGHT_DRAW =====
    val modifier: Modifier =
        Modifier.drawWithContent {
            val progress = pressProgressSpring.value
            if (progress > 0f) {
                if (shader != null) {
                    drawRect(
                        Color.White.copy(0.08f * progress),
                        blendMode = BlendMode.Plus,
                    )
                    shader.apply {
                        val position = position(size, animatedPosition)
                        setFloatUniform("size", size.width, size.height)
                        setColorUniform("color", Color.White.copy(0.15f * progress))
                        setFloatUniform("radius", size.minDimension * 1.5f)
                        setFloatUniform(
                            "position",
                            position.x.fastCoerceIn(0f, size.width),
                            position.y.fastCoerceIn(0f, size.height),
                        )
                    }
                    drawRect(
                        ShaderBrush(shader.asComposeShader()),
                        blendMode = BlendMode.Plus,
                    )
                } else {
                    drawRect(
                        Color.White.copy(flatGlowAlpha * progress),
                        blendMode = BlendMode.Plus,
                    )
                }
            }

            drawContent()
        }
    // ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT_DRAW =====

    // ===== SECTION: GLASS_INTERACTIVE_HIGHLIGHT_GESTURE =====
    val gestureModifier: Modifier =
        Modifier.pointerInput(animationScope) {
            inspectDragGestures(
                onDragStart = { down ->
                    startPosition = down.position
                    positionXSpring.snapTo(down.position.x)
                    positionYSpring.snapTo(down.position.y)
                    pressProgressSpring.animateTo(1f, pressSpring)
                },
                onDragEnd = { releaseToRest() },
                onDragCancel = { releaseToRest() },
            ) { change, _ ->
                // Retarget only: the running springs follow the finger without restarting.
                positionXSpring.animateTo(change.position.x, followSpring)
                positionYSpring.animateTo(change.position.y, followSpring)
            }
        }

    private fun releaseToRest() {
        pressProgressSpring.animateTo(0f, releaseSpring)
        positionXSpring.animateTo(startPosition.x, followSpring)
        positionYSpring.animateTo(startPosition.y, followSpring)
    }
    // ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT_GESTURE =====

    private companion object {
        /** Sub-pixel position threshold, in pixels. */
        const val POSITION_THRESHOLD = 0.5f
    }
}
// ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT =====
