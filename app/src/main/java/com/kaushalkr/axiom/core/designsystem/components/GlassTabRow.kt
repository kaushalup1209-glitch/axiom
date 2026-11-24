/*
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidBottomTabs.kt
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 * Changes for Axiom: Int selection API that reports only user-initiated changes (no
 * feedback loop with navigation), configurable heights and container colour so the same
 * component drives the floating navigation bar and segmented controls, theme colours,
 * Full/Lite/Off effect switching, a stronger resting container (wider lens rim, depth,
 * dispersion, rim light, sheen and inner shadow from GlassTokens), values moved to tokens,
 * springs scaled by the user's animation scale, and section labels. Pressed and dragged
 * rendering and the 1x drag physics are unchanged.
 */
package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kaushalkr.axiom.core.designsystem.glass.GlassIndicatorTokens
import com.kaushalkr.axiom.core.designsystem.glass.GlassSurfaceSpec
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.glassColorControls
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.interaction.DampedDragAnimation
import com.kaushalkr.axiom.core.designsystem.glass.interaction.InteractiveHighlight
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.backdrop.backdrops.layerBackdrop
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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

// ===== SECTION: GLASS_TAB_ROW =====
/**
 * A capsule of equally sized tabs with a draggable Liquid Glass selection indicator.
 *
 * Content is drawn twice: once normally and once tinted with the accent colour. In Full glass
 * the tinted copy is captured and shown only through the indicator, which becomes a
 * refracting, dispersive lens while pressed or dragged. Lite and Off share the same drag
 * physics but draw the indicator and clip the tinted copy directly ([LightTabRowLayers]),
 * with no capture or lens.
 *
 * @param selectedIndex currently selected tab.
 * @param onTabSelected called only when the user selects a different tab by dragging the
 *   indicator. Tab clicks are handled by each [GlassTab].
 * @param floating true for chrome that floats over content (adds shadow and chrome tint).
 * @param spec resting glass of the container; segmented controls pass [GlassTokens.SegmentedControl].
 */
@Composable
fun GlassTabRow(
    selectedIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    containerHeight: Dp = 64.dp,
    indicatorHeight: Dp = 56.dp,
    containerColor: Color = Color.Unspecified,
    indicatorColor: Color = Color.Unspecified,
    floating: Boolean = true,
    spec: GlassSurfaceSpec = GlassTokens.NavigationBar,
    content: @Composable RowScope.() -> Unit,
) {
    val style = LocalGlassStyle.current
    val motion = LocalAxiomMotion.current
    val colors = AxiomTheme.colors
    val backdrop = LocalGlassBackdrop.current
    val isLightTheme = !colors.isDark
    val accentColor = colors.accent
    val fullGlass = style.isFull
    val resolvedContainerColor = when {
        containerColor != Color.Unspecified -> containerColor
        !style.isEnabled -> style.solidSurfaceColor
        !fullGlass && style.usesBackdrop(spec) -> style.blurredChromeSurfaceColor
        else -> style.chromeSurfaceColor
    }
    // Catalog default: a faint shade drawn over the indicator (used by the navigation bar).
    val defaultIndicatorShade = if (isLightTheme) Color.Black.copy(0.1f) else Color.White.copy(0.1f)
    val containerPadding = (containerHeight - indicatorHeight) / 2

    val latestSelectedIndex by rememberUpdatedState(selectedIndex)
    val latestOnTabSelected by rememberUpdatedState(onTabSelected)

    // ===== SECTION: GLASS_TAB_ROW_RESTING_GLASS =====
    val containerHighlight = spec.edgeLight.toHighlight()
    val containerInnerShadow = if (style.usesRefraction) spec.depth?.toInnerShadow(style.isDark) else null
    val containerSheen = spec.sheenBrush(style.isDark)
    val containerRefraction = if (style.usesRefraction) spec.refraction else null
    // ===== END SECTION: GLASS_TAB_ROW_RESTING_GLASS =====

    BoxWithConstraints(
        modifier.semantics { selectableGroup() },
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - (containerPadding * 2).toPx()) / tabsCount
        }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    GlassIndicatorTokens.PanelOffsetMax.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()

        // ===== SECTION: GLASS_TAB_ROW_DRAG_ANIMATION =====
        val dampedDragAnimation = remember(animationScope, tabsCount, tabWidth, motion) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = latestSelectedIndex.toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = GlassIndicatorTokens.PRESSED_SCALE,
                motion = motion,
                onDragStarted = {},
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    animateToValue(targetIndex.toFloat())
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            motion.spring(AxiomMotionTokens.DragPanelReturn, 0.5f),
                        )
                    }
                    if (targetIndex != latestSelectedIndex) {
                        latestOnTabSelected(targetIndex)
                    }
                },
                onDrag = { _, dragAmount ->
                    updateValue(
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat()),
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                },
            )
        }
        LaunchedEffect(dampedDragAnimation) {
            // Follow external selection changes (tab clicks, navigation back) with the indicator.
            snapshotFlow { latestSelectedIndex }
                .drop(1)
                .collectLatest { index ->
                    dampedDragAnimation.animateToValue(index.toFloat())
                }
        }
        // ===== END SECTION: GLASS_TAB_ROW_DRAG_ANIMATION =====

        val interactiveHighlight = remember(animationScope, dampedDragAnimation, style.usesInteractiveLight, motion) {
            InteractiveHighlight(
                animationScope = animationScope,
                useShader = style.usesInteractiveLight,
                motion = motion,
                // Catalog springs keep the tab swipe light exactly as before.
                pressSpring = AxiomMotionTokens.CatalogHighlightPress,
                releaseSpring = AxiomMotionTokens.CatalogHighlightPress,
                followSpring = AxiomMotionTokens.CatalogHighlightFollow,
                position = { size, _ ->
                    Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f,
                    )
                },
            )
        }
        val highlightModifier = if (style.isEnabled) interactiveHighlight.modifier else Modifier
        val highlightGestureModifier = if (style.isEnabled) interactiveHighlight.gestureModifier else Modifier

        if (fullGlass) {
            // Full: the original Kyant0 rendering. The tinted copy is captured so the
            // indicator can refract it.
            val tabsBackdrop = rememberLayerBackdrop()

            // ===== SECTION: GLASS_TAB_ROW_CONTAINER =====
            Row(
                Modifier
                    .graphicsLayer {
                        translationX = panelOffset
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { Capsule() },
                        effects = {
                            if (style.isEnabled) {
                                glassColorControls(style.isDark)
                                blur(spec.blurRadius.toPx() * style.blurScale)
                            }
                            if (containerRefraction != null) {
                                cachedLens(
                                    containerRefraction.height.toPx(),
                                    containerRefraction.amount.toPx(),
                                    depthEffect = containerRefraction.depthEffect,
                                    chromaticAberration = containerRefraction.chromaticAberration,
                                )
                            }
                        },
                        highlight = if (style.isEnabled) ({ containerHighlight }) else null,
                        shadow = if (floating) spec.shadow?.let { value -> { value } } else null,
                        innerShadow = containerInnerShadow?.let { value -> { value } },
                        layerBlock = {
                            val progress = dampedDragAnimation.pressProgress
                            val scale = lerp(1f, 1f + GlassIndicatorTokens.PressedContainerGrowth.toPx() / size.width, progress)
                            scaleX = scale
                            scaleY = scale
                        },
                        onDrawSurface = {
                            drawRect(resolvedContainerColor)
                            if (style.isEnabled && containerSheen != null) drawRect(containerSheen)
                        },
                    )
                    .then(highlightModifier)
                    .height(containerHeight)
                    .fillMaxWidth()
                    .padding(containerPadding),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
            // ===== END SECTION: GLASS_TAB_ROW_CONTAINER =====

            // ===== SECTION: GLASS_TAB_ROW_TINTED_CONTENT =====
            CompositionLocalProvider(
                LocalGlassTabScale provides {
                    lerp(1f, GlassIndicatorTokens.PRESSED_CONTENT_SCALE, dampedDragAnimation.pressProgress)
                },
            ) {
                Row(
                    Modifier
                        .clearAndSetSemantics {}
                        .alpha(0f)
                        .layerBackdrop(tabsBackdrop)
                        .graphicsLayer {
                            translationX = panelOffset
                        }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { Capsule() },
                            effects = {
                                val progress = dampedDragAnimation.pressProgress
                                if (style.isEnabled) {
                                    glassColorControls(style.isDark)
                                    blur(spec.blurRadius.toPx() * style.blurScale)
                                }
                                if (containerRefraction != null) {
                                    // At rest the indicator shows the same rim bend as the container;
                                    // fully pressed it reaches the catalog lens used while swiping.
                                    val pressed = GlassIndicatorTokens.TintedContentPressedRefraction
                                    cachedLens(
                                        lerp(containerRefraction.height.toPx(), pressed.height.toPx(), progress),
                                        lerp(containerRefraction.amount.toPx(), pressed.amount.toPx(), progress),
                                    )
                                }
                            },
                            highlight = {
                                val progress = dampedDragAnimation.pressProgress
                                if (style.isEnabled) CachedHighlights.Default.copy(alpha = progress) else null
                            },
                            onDrawSurface = {
                                drawRect(resolvedContainerColor)
                                if (style.isEnabled && containerSheen != null) drawRect(containerSheen)
                                // Custom indicator fill (segmented controls): painted into this hidden
                                // copy at the indicator position, so it sits beneath the accent label
                                // the indicator shows. Full glass fades it on press into a clear lens.
                                if (indicatorColor != Color.Unspecified) {
                                    val progress = dampedDragAnimation.pressProgress
                                    val fillAlpha = if (style.usesRefraction) 1f - progress else 1f - 0.5f * progress
                                    val horizontalInset = containerPadding.toPx()
                                    val left =
                                        if (isLtr) horizontalInset + dampedDragAnimation.value * tabWidth
                                        else size.width - horizontalInset - (dampedDragAnimation.value + 1f) * tabWidth
                                    drawRect(
                                        color = indicatorColor,
                                        topLeft = Offset(left, 0f),
                                        size = Size(tabWidth, size.height),
                                        alpha = fillAlpha,
                                    )
                                }
                            },
                        )
                        .then(highlightModifier)
                        .height(indicatorHeight)
                        .fillMaxWidth()
                        .padding(horizontal = containerPadding)
                        .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            }
            // ===== END SECTION: GLASS_TAB_ROW_TINTED_CONTENT =====

            // ===== SECTION: GLASS_TAB_ROW_INDICATOR =====
            Box(
                Modifier
                    .padding(horizontal = containerPadding)
                    .graphicsLayer {
                        translationX =
                            if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                            else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                    }
                    .then(highlightGestureModifier)
                    .then(dampedDragAnimation.modifier)
                    .drawBackdrop(
                        backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                        shape = { Capsule() },
                        effects = {
                            val progress = dampedDragAnimation.pressProgress
                            if (style.usesRefraction) {
                                val pressed = GlassIndicatorTokens.PressedRefraction
                                cachedLens(
                                    pressed.height.toPx() * progress,
                                    pressed.amount.toPx() * progress,
                                    chromaticAberration = pressed.chromaticAberration,
                                )
                            }
                        },
                        highlight = {
                            val progress = dampedDragAnimation.pressProgress
                            if (style.isEnabled) CachedHighlights.Default.copy(alpha = progress) else null
                        },
                        shadow = {
                            val progress = dampedDragAnimation.pressProgress
                            Shadow(alpha = progress)
                        },
                        innerShadow = {
                            val progress = dampedDragAnimation.pressProgress
                            InnerShadow(
                                radius = GlassIndicatorTokens.PressedInnerShadowRadius * progress,
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
                            if (indicatorColor == Color.Unspecified) {
                                drawRect(defaultIndicatorShade, alpha = 1f - progress)
                            }
                            drawRect(Color.Black.copy(alpha = 0.03f * progress))
                        },
                    )
                    .height(indicatorHeight)
                    .fillMaxWidth(1f / tabsCount),
            )
            // ===== END SECTION: GLASS_TAB_ROW_INDICATOR =====
        } else {
            // ===== SECTION: GLASS_TAB_ROW_LIGHT =====
            LightTabRowLayers(
                style = style,
                spec = spec,
                floating = floating,
                containerHeight = containerHeight,
                indicatorHeight = indicatorHeight,
                containerPadding = containerPadding,
                containerColor = resolvedContainerColor,
                indicatorColor = if (indicatorColor != Color.Unspecified) indicatorColor else defaultIndicatorShade,
                tabsCount = tabsCount,
                tabWidth = tabWidth,
                isLtr = isLtr,
                panelOffset = { panelOffset },
                dragAnimation = dampedDragAnimation,
                // Lite keeps the flat press glow; Off has none.
                interactiveHighlight = if (style.isEnabled) interactiveHighlight else null,
                content = content,
            )
            // ===== END SECTION: GLASS_TAB_ROW_LIGHT =====
        }
    }
}
// ===== END SECTION: GLASS_TAB_ROW =====
