package com.kaushalkr.axiom.core.designsystem.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ===== SECTION: AXIOM_WIDTH_CLASS =====
/** Window width buckets matching the Material window size class breakpoints (600dp, 840dp). */
enum class AxiomWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
    ;

    /** True when two content columns fit comfortably. */
    val isWide: Boolean get() = this != COMPACT

    companion object {
        fun fromWidth(width: Dp): AxiomWidthClass = when {
            width < 600.dp -> COMPACT
            width < 840.dp -> MEDIUM
            else -> EXPANDED
        }
    }
}

val LocalAxiomWidthClass = staticCompositionLocalOf { AxiomWidthClass.COMPACT }
// ===== END SECTION: AXIOM_WIDTH_CLASS =====

// ===== SECTION: AXIOM_SCREEN_CONTENT_PADDING =====
/**
 * Space that screens must leave for floating chrome: status bar and top actions above,
 * the floating navigation bar (or system navigation area) below.
 * Provided by the app root so screens do not need to know which chrome is visible.
 */
val LocalScreenContentPadding = staticCompositionLocalOf { PaddingValues(0.dp) }
// ===== END SECTION: AXIOM_SCREEN_CONTENT_PADDING =====
