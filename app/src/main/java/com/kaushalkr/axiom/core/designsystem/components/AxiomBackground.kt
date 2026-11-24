package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import com.kaushalkr.axiom.core.designsystem.glass.GlassBackgroundTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import kotlin.math.roundToInt

// ===== SECTION: AXIOM_AMBIENT_BACKGROUND =====
/**
 * Static ambient canvas behind every screen: a base colour, three wide glows, three softer
 * defined orbs and a fine dot texture. The orbs and dots give Liquid Glass something to
 * bend, so refraction stays visible when nothing moves. Everything is built once per size in
 * [drawWithCache] (the dots are a single tiled shader), and nothing animates.
 */
@Composable
fun AxiomBackground(modifier: Modifier = Modifier) {
    val colors = AxiomTheme.colors
    val glowAlpha = if (colors.isDark) GlassBackgroundTokens.GLOW_ALPHA_DARK else GlassBackgroundTokens.GLOW_ALPHA_LIGHT
    val orbAlpha = if (colors.isDark) GlassBackgroundTokens.ORB_ALPHA_DARK else GlassBackgroundTokens.ORB_ALPHA_LIGHT

    Box(
        modifier.drawWithCache {
            val width = size.width
            val height = size.height

            // ===== SECTION: AXIOM_BACKGROUND_GLOWS =====
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
            // ===== END SECTION: AXIOM_BACKGROUND_GLOWS =====

            // ===== SECTION: AXIOM_BACKGROUND_ORBS =====
            val orbs = listOf(
                orbBrush(colors.backgroundGlowPrimary, orbAlpha, Offset(width * 0.84f, height * 0.22f), width * 0.30f),
                orbBrush(colors.backgroundGlowSecondary, orbAlpha, Offset(width * 0.10f, height * 0.52f), width * 0.26f),
                orbBrush(colors.backgroundGlowTertiary, orbAlpha, Offset(width * 0.80f, height * 0.78f), width * 0.28f),
            )
            // ===== END SECTION: AXIOM_BACKGROUND_ORBS =====

            val texture = dotTextureBrush(colors.backgroundTexture)

            onDrawBehind {
                drawRect(colors.background)
                drawRect(primaryGlow)
                drawRect(secondaryGlow)
                drawRect(tertiaryGlow)
                orbs.forEach { drawRect(it) }
                drawRect(texture)
            }
        },
    )
}
// ===== END SECTION: AXIOM_AMBIENT_BACKGROUND =====

// ===== SECTION: AXIOM_BACKGROUND_BRUSHES =====
/** A soft orb with a readable edge, so lens rims visibly bend its outline. */
private fun orbBrush(color: Color, alpha: Float, center: Offset, radius: Float): Brush =
    Brush.radialGradient(
        0f to color.copy(alpha = alpha),
        0.62f to color.copy(alpha = alpha * 0.72f),
        1f to Color.Transparent,
        center = center,
        radius = radius,
    )

/** One dot drawn into a small tile and repeated by the GPU across the whole screen. */
private fun CacheDrawScope.dotTextureBrush(color: Color): Brush {
    val spacing = GlassBackgroundTokens.TextureSpacing.toPx().roundToInt().coerceAtLeast(4)
    val tile = ImageBitmap(spacing, spacing)
    Canvas(tile).drawCircle(
        center = Offset(spacing / 2f, spacing / 2f),
        radius = GlassBackgroundTokens.TextureDotRadius.toPx(),
        paint = Paint().apply {
            this.color = color
            isAntiAlias = true
        },
    )
    return ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
}
// ===== END SECTION: AXIOM_BACKGROUND_BRUSHES =====
