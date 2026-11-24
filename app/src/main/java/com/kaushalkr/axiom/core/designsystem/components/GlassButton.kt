/*
 * Interaction and deformation logic adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidButton.kt
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 * Changes for Axiom: theme-driven colours, token-driven glass through glassSurface
 * (Full/Lite/Off), smoother scaled press springs, press deformation in every glass mode
 * instead of a ripple, enabled state, configurable shape and padding, content colour,
 * and section labels.
 */
package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kaushalkr.axiom.core.designsystem.glass.GlassPressTokens
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.glassSurface
import com.kaushalkr.axiom.core.designsystem.glass.interaction.InteractiveHighlight
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

// ===== SECTION: GLASS_BUTTON =====
/**
 * Liquid Glass button. On press it grows gently, stretches toward the finger and glows
 * (from the Backdrop catalog `LiquidButton`), then eases back with a soft spring. The same
 * press motion runs with glass off, on a solid surface.
 *
 * @param tint fills the button with a colour wash, e.g. the accent for primary actions.
 * @param contentColor colour for text and icons; defaults to on-accent when [tint] is set.
 * @param floating true for chrome over content: uses [GlassTokens.FloatingButton] and chrome tint.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    shape: Shape = GlassShapes.Capsule,
    minHeight: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    floating: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val style = LocalGlassStyle.current
    val motion = LocalAxiomMotion.current
    val colors = AxiomTheme.colors
    val backdrop = LocalGlassBackdrop.current

    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope, style.usesInteractiveLight, motion) {
        InteractiveHighlight(
            animationScope = animationScope,
            useShader = style.usesInteractiveLight,
            motion = motion,
            flatGlowAlpha = GlassPressTokens.FLAT_GLOW_ALPHA,
        )
    }

    val resolvedContentColor = when {
        contentColor.isSpecified -> contentColor
        tint.isSpecified -> colors.onAccent
        else -> colors.textPrimary
    }

    // ===== SECTION: GLASS_BUTTON_DEFORMATION =====
    val deformationLayerBlock: (GraphicsLayerScope.() -> Unit)? = remember(enabled, interactiveHighlight) {
        if (enabled) {
            {
                val width = size.width
                val height = size.height

                val progress = interactiveHighlight.pressProgress
                val scale = lerp(1f, 1f + GlassPressTokens.Growth.toPx() / size.height, progress)

                val maxOffset = size.minDimension
                val initialDerivative = GlassPressTokens.FOLLOW_DERIVATIVE
                val offset = interactiveHighlight.offset
                translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                val maxDragScale = GlassPressTokens.MaxStretch.toPx() / size.height
                val offsetAngle = atan2(offset.y, offset.x)
                scaleX =
                    scale +
                        maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                        (width / height).fastCoerceAtMost(1f)
                scaleY =
                    scale +
                        maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                        (height / width).fastCoerceAtMost(1f)
            }
        } else {
            null
        }
    }
    // ===== END SECTION: GLASS_BUTTON_DEFORMATION =====

    // Remembered so recomposition (e.g. a changing label) never rebuilds the glass pipeline.
    val surface = remember(backdrop, style, floating, shape, tint, deformationLayerBlock) {
        Modifier.glassSurface(
            backdrop = backdrop,
            style = style,
            spec = if (floating) GlassTokens.FloatingButton else GlassTokens.Button,
            shape = shape,
            surfaceColor = if (floating) style.chromeSurfaceColor else style.surfaceColor,
            tint = tint,
            layerBlock = deformationLayerBlock,
        )
    }

    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .then(surface)
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .then(
                if (enabled) {
                    Modifier
                        .then(interactiveHighlight.modifier)
                        .then(interactiveHighlight.gestureModifier)
                } else {
                    Modifier
                },
            )
            .defaultMinSize(minWidth = minHeight, minHeight = minHeight)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides resolvedContentColor,
            LocalTextStyle provides AxiomTheme.typography.labelLarge.copy(color = resolvedContentColor),
        ) {
            content()
        }
    }
}
// ===== END SECTION: GLASS_BUTTON =====
