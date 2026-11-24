/*
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidSlider.kt
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 * Changes for Axiom: value-based API with onValueChangeFinished, theme colours,
 * Full/Lite/Off effect switching, a 48dp gesture area covering the whole slider (tap to
 * jump, drag anywhere), slider semantics for accessibility, values moved to
 * GlassThumbTokens, springs scaled by the user's animation scale, and section labels.
 * Thumb rendering and 1x spring values are unchanged.
 */
package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kaushalkr.axiom.core.designsystem.glass.GlassThumbTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.glassBackdropSource
import com.kaushalkr.axiom.core.designsystem.glass.interaction.DampedDragAnimation
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kaushalkr.axiom.core.designsystem.glass.shader.cachedLens
import com.kaushalkr.axiom.core.designsystem.glass.shader.CachedHighlights
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.flow.collectLatest

// ===== SECTION: GLASS_SLIDER =====
/**
 * Liquid Glass slider. Drag anywhere on the control to adjust, or tap to jump.
 * [onValueChange] fires continuously; [onValueChangeFinished] fires once per gesture,
 * which is where callers should persist the value.
 */
@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: () -> Unit = {},
    contentDescription: String? = null,
) {
    val style = LocalGlassStyle.current
    val motion = LocalAxiomMotion.current
    val colors = AxiomTheme.colors
    val accentColor = colors.accent
    val trackColor = colors.controlTrack
    val thumbColor = colors.controlThumb
    val backdrop = LocalGlassBackdrop.current
    val visibilityThreshold = (valueRange.endInclusive - valueRange.start) / 1000f

    val latestValue by rememberUpdatedState(value)
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val latestOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)

    val trackBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(AxiomDimens.minTouchTarget),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = constraints.maxWidth.toFloat()

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var didDrag by remember { mutableStateOf(false) }
        var pressPositionX by remember { mutableFloatStateOf(0f) }

        // ===== SECTION: GLASS_SLIDER_DRAG_ANIMATION =====
        val dampedDragAnimation = remember(animationScope, trackWidth, motion) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = latestValue,
                valueRange = valueRange,
                visibilityThreshold = visibilityThreshold,
                initialScale = 1f,
                pressedScale = GlassThumbTokens.PRESSED_SCALE,
                motion = motion,
                onDragStarted = { position -> pressPositionX = position.x },
                onDragStopped = {
                    if (didDrag) {
                        latestOnValueChange(targetValue)
                        didDrag = false
                    } else {
                        // Tap: jump to the pressed position.
                        val fraction = (pressPositionX / trackWidth).fastCoerceIn(0f, 1f)
                        val span = valueRange.endInclusive - valueRange.start
                        val tapped =
                            if (isLtr) valueRange.start + span * fraction
                            else valueRange.endInclusive - span * fraction
                        animateToValue(tapped)
                        latestOnValueChange(tapped.coerceIn(valueRange))
                    }
                    latestOnValueChangeFinished()
                },
                onDrag = { _, dragAmount ->
                    if (!didDrag) {
                        didDrag = dragAmount.x != 0f
                    }
                    val delta = (valueRange.endInclusive - valueRange.start) * (dragAmount.x / trackWidth)
                    latestOnValueChange(
                        if (isLtr) (targetValue + delta).coerceIn(valueRange)
                        else (targetValue - delta).coerceIn(valueRange),
                    )
                },
            )
        }
        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { latestValue }
                .collectLatest { newValue ->
                    if (dampedDragAnimation.targetValue != newValue) {
                        dampedDragAnimation.updateValue(newValue)
                    }
                }
        }
        // ===== END SECTION: GLASS_SLIDER_DRAG_ANIMATION =====

        Box(
            Modifier
                .matchParentSize()
                .semantics {
                    if (contentDescription != null) this.contentDescription = contentDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(latestValue, valueRange)
                    setProgress { target ->
                        latestOnValueChange(target.coerceIn(valueRange))
                        latestOnValueChangeFinished()
                        true
                    }
                }
                .then(dampedDragAnimation.modifier),
        )

        // ===== SECTION: GLASS_SLIDER_TRACK =====
        Box(Modifier.glassBackdropSource(trackBackdrop, style.isEnabled)) {
            Box(
                Modifier
                    .clip(Capsule())
                    .background(trackColor)
                    .height(6f.dp)
                    .fillMaxWidth(),
            )

            Box(
                Modifier
                    .clip(Capsule())
                    .background(accentColor)
                    .height(6f.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val width = (constraints.maxWidth * dampedDragAnimation.progress).fastRoundToInt()
                        layout(width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    },
            )
        }
        // ===== END SECTION: GLASS_SLIDER_TRACK =====

        // ===== SECTION: GLASS_SLIDER_THUMB =====
        Box(
            Modifier
                .graphicsLayer {
                    translationX =
                        (-size.width / 2f + trackWidth * dampedDragAnimation.progress)
                            .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) *
                        if (isLtr) 1f else -1f
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = lerp(2f / 3f, 1f, progress)
                            val scaleY = lerp(0f, 1f, progress)
                            scale(scaleX, scaleY) {
                                drawBackdrop()
                            }
                        },
                    ),
                    shape = { Capsule() },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        blur(GlassThumbTokens.RestBlur.toPx() * (1f - progress) * style.blurScale)
                        if (style.usesRefraction) {
                            val refraction = GlassThumbTokens.SliderPressedRefraction
                            cachedLens(
                                refraction.height.toPx() * progress,
                                refraction.amount.toPx() * progress,
                                chromaticAberration = refraction.chromaticAberration,
                            )
                        }
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        if (style.isEnabled) {
                            CachedHighlights.Ambient.copy(
                                width = CachedHighlights.Ambient.width / 1.5f,
                                blurRadius = CachedHighlights.Ambient.blurRadius / 1.5f,
                                alpha = progress,
                            )
                        } else {
                            null
                        }
                    },
                    shadow = {
                        Shadow(
                            radius = 4f.dp,
                            color = Color.Black.copy(alpha = 0.05f),
                        )
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(
                            radius = GlassThumbTokens.PressedInnerShadowRadius * progress,
                            alpha = if (style.isEnabled) progress else 0f,
                        )
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        val surfaceAlpha = when {
                            style.usesRefraction -> 1f - progress
                            style.isEnabled -> 1f - 0.6f * progress
                            else -> 1f
                        }
                        drawRect(thumbColor.copy(alpha = surfaceAlpha))
                    },
                )
                .size(40f.dp, 24f.dp),
        )
        // ===== END SECTION: GLASS_SLIDER_THUMB =====
    }
}
// ===== END SECTION: GLASS_SLIDER =====
