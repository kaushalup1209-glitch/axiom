package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.glass.GlassRefraction
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.backdropOrEmpty
import com.kaushalkr.axiom.core.designsystem.glass.glassSurface
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: GLASS_CARD =====
/**
 * Content container rendered as Liquid Glass over [LocalGlassBackdrop].
 *
 * The card exports its own rendered surface as the backdrop for its children, so glass
 * controls placed inside (toggles, sliders, buttons) refract the card instead of the
 * screen behind it.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedRectangle(AxiomDimens.cardCornerRadius),
    contentPadding: PaddingValues = PaddingValues(AxiomSpacing.lg),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val style = LocalGlassStyle.current
    val backdrop = LocalGlassBackdrop.current
    val cardBackdrop = rememberLayerBackdrop()

    Column(
        modifier = modifier
            .glassSurface(
                backdrop = backdrop,
                style = style,
                shape = shape,
                blurRadius = 12.dp,
                refraction = GlassRefraction(height = 12.dp, amount = 24.dp),
                shadow = Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.06f)),
                exportedBackdrop = if (style.isEnabled) cardBackdrop else null,
            )
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
    ) {
        CompositionLocalProvider(
            LocalGlassBackdrop provides backdropOrEmpty(cardBackdrop, style.isEnabled),
        ) {
            content()
        }
    }
}
// ===== END SECTION: GLASS_CARD =====
