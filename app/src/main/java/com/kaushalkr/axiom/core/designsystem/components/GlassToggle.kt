/*
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidToggle.kt
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 * Changes for Axiom: Boolean state API, theme colours, Full/Lite/Off effect switching,
 * gesture area enlarged to the whole control with a 48dp touch target, switch semantics
 * for accessibility, values moved to GlassThumbTokens, springs scaled by the user's
 * animation scale, and section labels. Thumb rendering and 1x spring values are unchanged.
 */
package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kaushalkr.axiom.core.designsystem.glass.GlassThumbTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.glassBackdropSource
import com.kaushalkr.axiom.core.designsystem.glass.interaction.DampedDragAnimation
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
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

// ===== SECTION: GLASS_TOGGLE =====
/**
 * Liquid Glass switch. Tap anywhere on the control to toggle, or drag the thumb.
 * In Full glass the pressed thumb becomes a refracting lens over the track.
 */
@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val style = LocalGlassStyle.current
    val motion = LocalAxiomMotion.current
    val colors = AxiomTheme.colors
    val accentColor = colors.accent
    val trackColor = colors.controlTrack
    val thumbColor = colors.controlThumb
    val backdrop = LocalGlassBackdrop.current

    val latestChecked by rememberUpdatedState(checked)
    val latestOnCheckedChange by rememberUpdatedState(onCheckedChange)

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { 20f.dp.toPx() }
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (checked) 1f else 0f) }

    // ===== SECTION: GLASS_TOGGLE_DRAG_ANIMATION =====
    val dampedDragAnimation = remember(animationScope, motion) {
        DampedDragAnimation(
            animationScope = animationScope,
            // Read the parameter, not the `fraction` state, so dragging never recomposes.
            initialValue = if (checked) 1f else 0f,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = GlassThumbTokens.PRESSED_SCALE,
            motion = motion,
            onDragStarted = {},
            onDragStopped = {
                if (didDrag) {
                    fraction = if (targetValue >= 0.5f) 1f else 0f
                    latestOnCheckedChange(fraction == 1f)
                    didDrag = false
                } else {
                    fraction = if (latestChecked) 0f else 1f
                    latestOnCheckedChange(fraction == 1f)
                }
            },
            onDrag = { _, dragAmount ->
                if (!didDrag) {
                    didDrag = dragAmount.x != 0f
                }
                val delta = dragAmount.x / dragWidth
                fraction =
                    if (isLtr) (fraction + delta).fastCoerceIn(0f, 1f)
                    else (fraction - delta).fastCoerceIn(0f, 1f)
            },
        )
    }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { fraction }
            .collectLatest { value ->
                dampedDragAnimation.updateValue(value)
            }
    }
    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { latestChecked }
            .collectLatest { isChecked ->
                val target = if (isChecked) 1f else 0f
                if (target != fraction) {
                    fraction = target
                    dampedDragAnimation.animateToValue(target)
                }
            }
    }
    // ===== END SECTION: GLASS_TOGGLE_DRAG_ANIMATION =====

    val trackBackdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .semantics {
                role = Role.Switch
                toggleableState = ToggleableState(latestChecked)
                if (contentDescription != null) this.contentDescription = contentDescription
                onClick {
                    latestOnCheckedChange(!latestChecked)
                    true
                }
            }
            .then(dampedDragAnimation.modifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        // ===== SECTION: GLASS_TOGGLE_TRACK =====
        Box(
            Modifier
                .glassBackdropSource(trackBackdrop, style.isEnabled)
                .clip(Capsule())
                .drawBehind {
                    drawRect(lerp(trackColor, accentColor, dampedDragAnimation.value))
                }
                .size(64f.dp, 28f.dp),
        )
        // ===== END SECTION: GLASS_TOGGLE_TRACK =====

        // ===== SECTION: GLASS_TOGGLE_THUMB =====
        Box(
            Modifier
                .graphicsLayer {
                    val value = dampedDragAnimation.value
                    val padding = 2f.dp.toPx()
                    translationX =
                        if (isLtr) lerp(padding, padding + dragWidth, value)
                        else lerp(-padding, -(padding + dragWidth), value)
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = lerp(2f / 3f, 0.75f, progress)
                            val scaleY = lerp(0f, 0.75f, progress)
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
                            val refraction = GlassThumbTokens.TogglePressedRefraction
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
                        val velocity = dampedDragAnimation.velocity / 50f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        // Full glass turns the pressed thumb into a clear lens; Lite keeps a frost;
                        // Off keeps an opaque thumb.
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
        // ===== END SECTION: GLASS_TOGGLE_THUMB =====
    }
}
// ===== END SECTION: GLASS_TOGGLE =====
