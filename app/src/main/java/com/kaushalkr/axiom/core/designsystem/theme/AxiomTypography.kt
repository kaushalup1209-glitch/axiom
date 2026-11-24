package com.kaushalkr.axiom.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// ===== SECTION: AXIOM_TYPOGRAPHY =====
/**
 * Type scale tuned for glass surfaces: slightly heavier weights and tighter display
 * tracking keep text legible over refracted backgrounds.
 */
private val AxiomFontFamily = FontFamily.Default

val AxiomTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 58.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    displaySmall = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.02).em,
    ),
    headlineSmall = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.01).em,
    ),
    titleLarge = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).em,
    ),
    titleMedium = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    ),
    labelSmall = TextStyle(
        fontFamily = AxiomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)
// ===== END SECTION: AXIOM_TYPOGRAPHY =====

// ===== SECTION: AXIOM_TYPOGRAPHY_NUMERIC =====
private const val TABULAR_NUMBERS = "tnum"

/** Equal-width digits, so changing numbers (timers, totals) do not shift sideways. */
fun TextStyle.tabularNumbers(): TextStyle = copy(fontFeatureSettings = TABULAR_NUMBERS)
// ===== END SECTION: AXIOM_TYPOGRAPHY_NUMERIC =====
