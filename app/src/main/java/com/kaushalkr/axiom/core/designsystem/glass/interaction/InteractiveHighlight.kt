/*
 * Copyright 2025 Kyant
 * Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 *
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/utils/InteractiveHighlight.kt
 * Changes for Axiom: package name, a [useShader] switch so Lite and Off skip the
 * RuntimeShader light, configurable press/release/follow springs scaled by the user's
 * animation scale (buttons use smoother springs; the tab bar keeps the catalog springs),
 * a configurable flat glow, and section labels. The shader source is unchanged.
 */
package com.kaushalkr.axiom.core.designsystem.glass.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
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
import com.kaushalkr.axiom.core.designsystem.motion.SpringSpecValues
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.isRuntimeShaderSupported
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    pressSpring: SpringSpecValues = AxiomMotionTokens.PressIn,
    releaseSpring: SpringSpecValues = AxiomMotionTokens.PressRelease,
    followSpring: SpringSpecValues = AxiomMotionTokens.PressFollow,
    private val flatGlowAlpha: Float = 0.25f,
    val position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset },
) {

    // ===== SECTION: GLASS_INTERACTIVE_HIGHLIGHT_SPECS =====
    private val pressAnimationSpec = motion.spring(pressSpring, 0.001f)
    private val releaseAnimationSpec = motion.spring(releaseSpring, 0.001f)
    private val positionAnimationSpec = motion.spring(followSpring, Offset.VisibilityThreshold)
    // ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT_SPECS =====

    private val pressProgressAnimation =
        Animatable(0f, 0.001f)
    private val positionAnimation =
        Animatable(Offset.Zero, Offset.VectorConverter, Offset.VisibilityThreshold)

    private var startPosition = Offset.Zero
    val pressProgress: Float get() = pressProgressAnimation.value
    val offset: Offset get() = positionAnimation.value - startPosition

    // ===== SECTION: GLASS_INTERACTIVE_HIGHLIGHT_SHADER =====
    private val shader =
        if (useShader && isRuntimeShaderSupported()) {
            RuntimeShader(
                """
uniform float2 size;
layout(color) uniform half4 color;
uniform float radius;
uniform float2 position;

half4 main(float2 coord) {
    float dist = distance(coord, position);
    float intensity = smoothstep(radius, radius * 0.5, dist);
    return color * intensity;
}""",
            )
        } else {
            null
        }
    // ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT_SHADER =====

    // ===== SECTION: GLASS_INTERACTIVE_HIGHLIGHT_DRAW =====
    val modifier: Modifier =
        Modifier.drawWithContent {
            val progress = pressProgressAnimation.value
            if (progress > 0f) {
                if (shader != null) {
                    drawRect(
                        Color.White.copy(0.08f * progress),
                        blendMode = BlendMode.Plus,
                    )
                    shader.apply {
                        val position = position(size, positionAnimation.value)
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
                    animationScope.launch {
                        launch { pressProgressAnimation.animateTo(1f, pressAnimationSpec) }
                        launch { positionAnimation.snapTo(startPosition) }
                    }
                },
                onDragEnd = {
                    animationScope.launch {
                        launch { pressProgressAnimation.animateTo(0f, releaseAnimationSpec) }
                        launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
                    }
                },
                onDragCancel = {
                    animationScope.launch {
                        launch { pressProgressAnimation.animateTo(0f, releaseAnimationSpec) }
                        launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
                    }
                },
            ) { change, _ ->
                // Follow the finger through a spring so deformation flows instead of snapping.
                animationScope.launch { positionAnimation.animateTo(change.position, positionAnimationSpec) }
            }
        }
    // ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT_GESTURE =====
}
// ===== END SECTION: GLASS_INTERACTIVE_HIGHLIGHT =====
