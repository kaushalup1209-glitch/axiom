package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassSurfaceSpec
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.glassSurface
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

// ===== SECTION: GLASS_CARD =====
/**
 * Content container rendered as Liquid Glass over [LocalGlassBackdrop].
 *
 * When [hostsGlass] is true and glass is Full, the card exports its own rendered surface as the
 * backdrop for its children, so glass controls placed inside (toggles, sliders, buttons)
 * refract the card instead of the screen behind it. Leave it false for cards without glass
 * children: exporting records an extra layer on every redraw. Lite and Off never export.
 *
 * @param spec glass appearance; dialogs pass [GlassTokens.Dialog].
 * @param onClick makes the whole card a button with the soft press overlay.
 * @param surfaceColor tint over the backdrop; defaults to the card tint.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassShapes.Card,
    spec: GlassSurfaceSpec = GlassTokens.Card,
    surfaceColor: Color = LocalGlassStyle.current.surfaceColor,
    contentPadding: PaddingValues = PaddingValues(AxiomSpacing.lg),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    hostsGlass: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val style = LocalGlassStyle.current
    val backdrop = LocalGlassBackdrop.current
    // Only Full glass refracts children through the card; Lite children are lightweight glass,
    // so neither list rows nor Lite cards allocate or record an extra layer.
    val exportsBackdrop = hostsGlass && style.exportsNestedBackdrops
    val cardBackdrop = if (exportsBackdrop) rememberLayerBackdrop() else null

    // Remembered so recomposing the card's content never rebuilds its glass pipeline.
    val surface = remember(backdrop, style, spec, shape, surfaceColor, exportsBackdrop, cardBackdrop) {
        Modifier.glassSurface(
            backdrop = backdrop,
            style = style,
            spec = spec,
            shape = shape,
            surfaceColor = surfaceColor,
            exportedBackdrop = if (exportsBackdrop) cardBackdrop else null,
        )
    }
    val childBackdrop = if (exportsBackdrop && cardBackdrop != null) cardBackdrop else emptyBackdrop()

    Column(
        modifier = modifier
            .then(surface)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
    ) {
        CompositionLocalProvider(
            LocalGlassBackdrop provides childBackdrop,
        ) {
            content()
        }
    }
}
// ===== END SECTION: GLASS_CARD =====
