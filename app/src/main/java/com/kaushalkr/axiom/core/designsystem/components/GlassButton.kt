/*
 * Interaction and deformation logic adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidButton.kt
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 * Changes for Axiom: theme-driven colours, Full/Lite/Off rendering through glassSurface,
 * enabled state, configurable shape and padding, content colour, and section labels.
 */
package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.LocalIndication
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
import com.kaushalkr.axiom.core.designsystem.glass.GlassRefraction
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.glassSurface
import com.kaushalkr.axiom.core.designsystem.glass.interaction.InteractiveHighlight
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

// ===== SECTION: GLASS_BUTTON =====
/**
 * Liquid Glass button. In Full and Lite glass it stretches toward the finger and glows on
 * press (from the Backdrop catalog `LiquidButton`); with glass off it uses a ripple.
 *
 * @param tint fills the button with a colour wash, e.g. the accent for primary actions.
 * @param contentColor colour for text and icons; defaults to on-accent when [tint] is set.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    shape: Shape = Capsule(),
    minHeight: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    floating: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val style = LocalGlassStyle.current
    val colors = AxiomTheme.colors
    val backdrop = LocalGlassBackdrop.current
    val isInteractive = enabled && style.isEnabled

    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope, style.usesInteractiveLight) {
        InteractiveHighlight(
            animationScope = animationScope,
            useShader = style.usesInteractiveLight,
        )
    }

    val resolvedContentColor = when {
        contentColor.isSpecified -> contentColor
        tint.isSpecified -> colors.onAccent
        else -> colors.textPrimary
    }

    // ===== SECTION: GLASS_BUTTON_DEFORMATION =====
    val deformationLayerBlock: (GraphicsLayerScope.() -> Unit)? =
        if (isInteractive) {
            {
                val width = size.width
                val height = size.height

                val progress = interactiveHighlight.pressProgress
                val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

                val maxOffset = size.minDimension
                val initialDerivative = 0.05f
                val offset = interactiveHighlight.offset
                translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                val maxDragScale = 4f.dp.toPx() / size.height
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
    // ===== END SECTION: GLASS_BUTTON_DEFORMATION =====

    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .glassSurface(
                backdrop = backdrop,
                style = style,
                shape = shape,
                blurRadius = 2.dp,
                refraction = GlassRefraction(height = 12.dp, amount = 24.dp),
                surfaceColor = if (floating) style.chromeSurfaceColor else style.surfaceColor,
                tint = tint,
                shadow = if (floating) Shadow.Default else Shadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.06f)),
                fallbackElevation = if (floating) 6.dp else 0.dp,
                layerBlock = deformationLayerBlock,
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = if (isInteractive) null else LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            )
            .then(
                if (isInteractive) {
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
