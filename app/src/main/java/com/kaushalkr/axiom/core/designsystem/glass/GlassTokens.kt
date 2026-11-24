package com.kaushalkr.axiom.core.designsystem.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.model.GlassMode
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.effects.colorControls
import com.kaushalkr.axiom.core.designsystem.glass.shader.CachedHighlights
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: GLASS_SPEC_TYPES =====
/** Lens parameters for the Backdrop `lens` effect. Rendered only in Full glass. */
@Immutable
data class GlassRefraction(
    /** Width of the refracting rim, measured inward from the edge. */
    val height: Dp,
    /** Maximum displacement at the very edge. */
    val amount: Dp,
    /** Bends the whole surface toward its centre for a thicker, domed look. */
    val depthEffect: Boolean = false,
    /** Splits colours slightly at the rim (prism-like dispersion). */
    val chromaticAberration: Boolean = false,
)

/** Specular rim light drawn with the Backdrop `Highlight` shader. */
@Immutable
data class GlassEdgeLight(
    val width: Dp,
    val intensity: Float,
    val angle: Float = 45f,
    val falloff: Float = 1f,
) {
    fun toHighlight(alpha: Float = 1f): Highlight =
        CachedHighlights.edge(width = width, intensity = intensity, angle = angle, falloff = falloff, alpha = alpha)
}

/** Inner shadow that gives glass visible thickness. Rendered only in Full glass. */
@Immutable
data class GlassDepth(
    val radius: Dp,
    val lightAlpha: Float,
    val darkAlpha: Float,
) {
    fun toInnerShadow(isDark: Boolean): InnerShadow = InnerShadow(
        radius = radius,
        offset = DpOffset(0.dp, radius / 3f),
        color = Color.Black.copy(alpha = if (isDark) darkAlpha else lightAlpha),
    )
}

/**
 * How much rendering budget a surface gets in Lite glass. Full glass renders every surface
 * the same way; Off renders none.
 */
enum class GlassEmphasis {
    /** Cards, list rows, buttons, inputs and controls: lightweight glass in Lite. */
    STANDARD,

    /**
     * The one or two surfaces a screen is built around (navigation bar, dialogs, sheets):
     * Lite blurs the screen behind them. Keep this to at most two visible at once.
     */
    PROMINENT,
}

/** Complete appearance of one kind of glass surface. */
@Immutable
data class GlassSurfaceSpec(
    val blurRadius: Dp,
    val refraction: GlassRefraction?,
    val edgeLight: GlassEdgeLight,
    /** Strength of the top-down reflection band in light and dark themes. */
    val sheenLight: Float,
    val sheenDark: Float,
    val depth: GlassDepth?,
    val shadow: Shadow?,
    /** Hardware elevation shadow used wherever the surface is drawn without Full glass. */
    val fallbackElevation: Dp = 0.dp,
    val emphasis: GlassEmphasis = GlassEmphasis.STANDARD,
) {
    /** A size-independent vertical reflection gradient; Compose caches its shader per size. */
    fun sheenBrush(isDark: Boolean): Brush? {
        val strength = if (isDark) sheenDark else sheenLight
        if (strength <= 0f) return null
        return Brush.verticalGradient(
            0f to Color.White.copy(alpha = strength),
            0.45f to Color.White.copy(alpha = strength * 0.25f),
            0.7f to Color.Transparent,
        )
    }
}
// ===== END SECTION: GLASS_SPEC_TYPES =====

// ===== SECTION: GLASS_SHAPE_TOKENS =====
/**
 * Shared shape instances. Reusing the same instance lets glass modifiers be remembered and
 * compared by identity, so recomposition does not rebuild the glass pipeline.
 */
object GlassShapes {
    val Capsule: Shape = com.kyant.shapes.Capsule()
    val Card: Shape = RoundedRectangle(AxiomDimens.cardCornerRadius)
    val Tile: Shape = RoundedRectangle(AxiomDimens.tileCornerRadius)
    val Dialog: Shape = RoundedRectangle(AxiomDimens.dialogCornerRadius)
    val Input: Shape = RoundedRectangle(AxiomDimens.inputCornerRadius)
    val Sheet: Shape = RoundedRectangle(AxiomDimens.dialogCornerRadius)
}
// ===== END SECTION: GLASS_SHAPE_TOKENS =====

// ===== SECTION: GLASS_SURFACE_TOKENS =====
/**
 * Every Liquid Glass surface in Axiom, defined once. Tune glass here, not in components.
 * Refraction rims are wide and blur is low so the background stays visibly distorted at
 * rest, while tint (see [GlassTintTokens]) keeps text readable.
 */
object GlassTokens {

    val Card = GlassSurfaceSpec(
        blurRadius = 6.dp,
        refraction = GlassRefraction(height = 24.dp, amount = 44.dp, depthEffect = true),
        edgeLight = GlassEdgeLight(width = 1.dp, intensity = 0.85f),
        sheenLight = 0.22f,
        sheenDark = 0.08f,
        depth = GlassDepth(radius = 14.dp, lightAlpha = 0.07f, darkAlpha = 0.22f),
        shadow = Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.07f)),
    )

    val Dialog = GlassSurfaceSpec(
        blurRadius = 10.dp,
        refraction = GlassRefraction(height = 28.dp, amount = 56.dp, depthEffect = true),
        edgeLight = GlassEdgeLight(width = 1.2.dp, intensity = 0.9f),
        sheenLight = 0.24f,
        sheenDark = 0.09f,
        depth = GlassDepth(radius = 18.dp, lightAlpha = 0.08f, darkAlpha = 0.25f),
        shadow = Shadow(radius = 32.dp, color = Color.Black.copy(alpha = 0.14f)),
        fallbackElevation = 12.dp,
        emphasis = GlassEmphasis.PROMINENT,
    )

    val Button = GlassSurfaceSpec(
        blurRadius = 2.dp,
        refraction = GlassRefraction(height = 14.dp, amount = 30.dp),
        edgeLight = GlassEdgeLight(width = 1.dp, intensity = 0.8f),
        sheenLight = 0.26f,
        sheenDark = 0.10f,
        depth = GlassDepth(radius = 8.dp, lightAlpha = 0.06f, darkAlpha = 0.20f),
        shadow = Shadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.06f)),
    )

    val FloatingButton = GlassSurfaceSpec(
        blurRadius = 2.dp,
        refraction = GlassRefraction(height = 18.dp, amount = 36.dp, depthEffect = true, chromaticAberration = true),
        edgeLight = GlassEdgeLight(width = 1.dp, intensity = 0.9f),
        sheenLight = 0.28f,
        sheenDark = 0.10f,
        depth = GlassDepth(radius = 10.dp, lightAlpha = 0.07f, darkAlpha = 0.22f),
        shadow = Shadow.Default,
        fallbackElevation = 6.dp,
    )

    val NavigationBar = GlassSurfaceSpec(
        blurRadius = 5.dp,
        refraction = GlassRefraction(height = 26.dp, amount = 44.dp, depthEffect = true, chromaticAberration = true),
        edgeLight = GlassEdgeLight(width = 1.dp, intensity = 0.9f),
        sheenLight = 0.22f,
        sheenDark = 0.08f,
        depth = GlassDepth(radius = 12.dp, lightAlpha = 0.06f, darkAlpha = 0.20f),
        shadow = Shadow.Default,
        fallbackElevation = 8.dp,
        emphasis = GlassEmphasis.PROMINENT,
    )

    /**
     * Rows in long scrolling lists (task cards). Many are on screen and all redraw while
     * scrolling, so this drops the two blur-based extras (drop shadow and inner depth shadow)
     * and keeps the lens rim, rim light and sheen that carry the glass look.
     */
    val ListItem = GlassSurfaceSpec(
        blurRadius = 4.dp,
        refraction = GlassRefraction(height = 18.dp, amount = 34.dp, depthEffect = true),
        edgeLight = GlassEdgeLight(width = 0.8.dp, intensity = 0.8f),
        sheenLight = 0.18f,
        sheenDark = 0.07f,
        depth = null,
        shadow = null,
    )

    /** Text inputs placed directly on the screen (search, task title). */
    val Input = GlassSurfaceSpec(
        blurRadius = 3.dp,
        refraction = GlassRefraction(height = 14.dp, amount = 26.dp),
        edgeLight = GlassEdgeLight(width = 0.8.dp, intensity = 0.75f),
        sheenLight = 0.14f,
        sheenDark = 0.06f,
        depth = GlassDepth(radius = 8.dp, lightAlpha = 0.05f, darkAlpha = 0.18f),
        shadow = null,
    )

    /**
     * Bottom sheets (filters, sort, date and time pickers). Blurred more than dialogs, because
     * a sheet covers busy content and holds dense controls such as the calendar grid.
     */
    val Sheet = GlassSurfaceSpec(
        blurRadius = 16.dp,
        refraction = GlassRefraction(height = 28.dp, amount = 56.dp, depthEffect = true),
        edgeLight = GlassEdgeLight(width = 1.2.dp, intensity = 0.9f),
        sheenLight = 0.22f,
        sheenDark = 0.08f,
        depth = GlassDepth(radius = 18.dp, lightAlpha = 0.07f, darkAlpha = 0.24f),
        shadow = Shadow(radius = 32.dp, color = Color.Black.copy(alpha = 0.16f)),
        fallbackElevation = 16.dp,
        emphasis = GlassEmphasis.PROMINENT,
    )

    /** The floating undo snackbar. */
    val Snackbar = GlassSurfaceSpec(
        blurRadius = 6.dp,
        refraction = GlassRefraction(height = 20.dp, amount = 36.dp, depthEffect = true),
        edgeLight = GlassEdgeLight(width = 1.dp, intensity = 0.9f),
        sheenLight = 0.22f,
        sheenDark = 0.08f,
        depth = GlassDepth(radius = 10.dp, lightAlpha = 0.06f, darkAlpha = 0.20f),
        shadow = Shadow.Default,
        fallbackElevation = 8.dp,
    )

    val SegmentedControl = GlassSurfaceSpec(
        blurRadius = 2.dp,
        refraction = GlassRefraction(height = 16.dp, amount = 28.dp),
        edgeLight = GlassEdgeLight(width = 0.8.dp, intensity = 0.7f),
        sheenLight = 0f,
        sheenDark = 0f,
        depth = GlassDepth(radius = 8.dp, lightAlpha = 0.05f, darkAlpha = 0.18f),
        shadow = null,
    )
}
// ===== END SECTION: GLASS_SURFACE_TOKENS =====

// ===== SECTION: GLASS_INTERACTION_TOKENS =====
/**
 * Pressed and dragged states of the selection indicator and control thumbs. These are the
 * Backdrop catalog values behind the tab-bar swipe effect; keep them unless that motion
 * should change.
 */
object GlassIndicatorTokens {
    val PressedRefraction = GlassRefraction(height = 10.dp, amount = 14.dp, chromaticAberration = true)
    val TintedContentPressedRefraction = GlassRefraction(height = 24.dp, amount = 24.dp)
    const val PRESSED_SCALE = 78f / 56f
    const val PRESSED_CONTENT_SCALE = 1.2f
    val PressedContainerGrowth = 16.dp
    val PanelOffsetMax = 4.dp
    val PressedInnerShadowRadius = 8.dp
}

object GlassThumbTokens {
    val RestBlur = 8.dp
    const val PRESSED_SCALE = 1.5f
    val TogglePressedRefraction = GlassRefraction(height = 5.dp, amount = 10.dp, chromaticAberration = true)
    val SliderPressedRefraction = GlassRefraction(height = 10.dp, amount = 14.dp, chromaticAberration = true)
    val PressedInnerShadowRadius = 4.dp
}

/** Press deformation for glass buttons (stretch toward the finger and gentle growth). */
object GlassPressTokens {
    val Growth = 4.dp
    val MaxStretch = 4.dp
    const val FOLLOW_DERIVATIVE = 0.05f
    const val FLAT_GLOW_ALPHA = 0.12f
}
// ===== END SECTION: GLASS_INTERACTION_TOKENS =====

// ===== SECTION: GLASS_BACKGROUND_TOKENS =====
/**
 * Ambient background detail. Glass can only show refraction where there is something to
 * bend, so the background carries soft orbs and a fine dot texture behind every surface.
 */
object GlassBackgroundTokens {
    const val GLOW_ALPHA_LIGHT = 0.42f
    const val GLOW_ALPHA_DARK = 0.55f
    const val ORB_ALPHA_LIGHT = 0.38f
    const val ORB_ALPHA_DARK = 0.50f
    val TextureSpacing = 18.dp
    val TextureDotRadius = 1.1.dp
}
// ===== END SECTION: GLASS_BACKGROUND_TOKENS =====

// ===== SECTION: GLASS_TINT_TOKENS =====
/** Surface tint ranges (mapped from the 0..1 tint setting) and backdrop colour treatment. */
object GlassTintTokens {
    /** Cards and controls. Lite surfaces have no blur behind them, so they carry more tint. */
    fun surfaceAlphaRange(mode: GlassMode): ClosedFloatingPointRange<Float> = when (mode) {
        GlassMode.FULL -> 0.04f..0.62f
        GlassMode.LITE -> 0.58f..0.92f
        GlassMode.OFF -> 1f..1f
    }

    /** Floating chrome drawn without a backdrop (top buttons, undo bar). */
    fun chromeAlphaRange(mode: GlassMode): ClosedFloatingPointRange<Float> = when (mode) {
        GlassMode.FULL -> 0.10f..0.62f
        GlassMode.LITE -> 0.68f..0.94f
        GlassMode.OFF -> 1f..1f
    }

    /** Lite's prominent chrome over a blurred backdrop (navigation bar). */
    val LiteBlurredChromeAlphaRange: ClosedFloatingPointRange<Float> = 0.30f..0.84f

    /** Dialogs carry long text, so their tint never drops below this. */
    const val DIALOG_MIN_ALPHA = 0.5f

    /** Lite compensates for the missing refraction with stronger frost on its blurred surfaces. */
    const val LITE_BLUR_SCALE = 1.5f

    const val BACKDROP_SATURATION = 1.5f
    const val BACKDROP_LIGHT_BRIGHTNESS = 0.04f
}

/** Saturation (and, in light theme, a small lift) applied to everything seen through glass. */
fun BackdropEffectScope.glassColorControls(isDark: Boolean) {
    colorControls(
        brightness = if (isDark) 0f else GlassTintTokens.BACKDROP_LIGHT_BRIGHTNESS,
        saturation = GlassTintTokens.BACKDROP_SATURATION,
    )
}
// ===== END SECTION: GLASS_TINT_TOKENS =====

// ===== SECTION: GLASS_LITE_TOKENS =====
/**
 * Lightweight glass used by Lite for standard surfaces: a translucent vertical gradient, the
 * surface's sheen band and a thin light-catching rim. Plain draw operations only, with no
 * backdrop, blur, shader or offscreen layer.
 */
object GlassLiteTokens {
    /** Bottom of the fill gradient relative to the top alpha, for a hint of depth. */
    const val FILL_BOTTOM_FACTOR = 0.84f

    /** Accent-tinted surfaces (primary buttons). */
    const val TINT_TOP_ALPHA = 0.96f
    const val TINT_BOTTOM_ALPHA = 0.86f

    val RimWidth = 1.dp

    /** Rim gradient, top to bottom: bright edge, softer sides, faint shade at the base. */
    val RimLight = listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.40f), Color(0xFF0E1117).copy(alpha = 0.07f))
    val RimDark = listOf(Color.White.copy(alpha = 0.24f), Color.White.copy(alpha = 0.09f), Color.White.copy(alpha = 0.04f))

    /** Elevation of control thumbs (toggle, slider) when they are not Full glass lenses. */
    val ThumbElevation = 2.dp

    /** A pressed thumb frosts slightly instead of turning into a lens. */
    const val THUMB_PRESSED_FROST = 0.22f

    /** Subtle rim on the selected segment of segmented controls and the navigation bar. */
    const val INDICATOR_RIM_ALPHA_LIGHT = 0.70f
    const val INDICATOR_RIM_ALPHA_DARK = 0.14f
}
// ===== END SECTION: GLASS_LITE_TOKENS =====
