package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: AXIOM_AMBIENT_BACKGROUND =====
/**
 * Static ambient canvas behind every screen: a base colour with three soft colour glows.
 * Glass cards refract this layer. It is drawn from cached brushes and never animates,
 * so it costs nothing between frames.
 */
@Composable
fun AxiomBackground(modifier: Modifier = Modifier) {
    val colors = AxiomTheme.colors
    val glowAlpha = if (colors.isDark) 0.55f else 0.42f

    Box(
        modifier.drawWithCache {
            val width = size.width
            val height = size.height
            val primaryGlow = Brush.radialGradient(
                colors = listOf(colors.backgroundGlowPrimary.copy(alpha = glowAlpha), Color.Transparent),
                center = Offset(width * 0.05f, height * 0.06f),
                radius = width * 0.95f,
            )
            val secondaryGlow = Brush.radialGradient(
                colors = listOf(colors.backgroundGlowSecondary.copy(alpha = glowAlpha * 0.85f), Color.Transparent),
                center = Offset(width * 1.02f, height * 0.42f),
                radius = width * 0.85f,
            )
            val tertiaryGlow = Brush.radialGradient(
                colors = listOf(colors.backgroundGlowTertiary.copy(alpha = glowAlpha * 0.75f), Color.Transparent),
                center = Offset(width * 0.1f, height * 0.92f),
                radius = width * 0.9f,
            )
            onDrawBehind {
                drawRect(colors.background)
                drawRect(primaryGlow)
                drawRect(secondaryGlow)
                drawRect(tertiaryGlow)
            }
        },
    )
}
// ===== END SECTION: AXIOM_AMBIENT_BACKGROUND =====
