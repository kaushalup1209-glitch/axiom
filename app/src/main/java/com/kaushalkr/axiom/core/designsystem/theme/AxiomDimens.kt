package com.kaushalkr.axiom.core.designsystem.theme

import androidx.compose.ui.unit.dp

// ===== SECTION: AXIOM_SPACING =====
/** 4dp-based spacing scale shared by every screen. */
object AxiomSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}
// ===== END SECTION: AXIOM_SPACING =====

// ===== SECTION: AXIOM_DIMENS =====
/** Fixed component dimensions. */
object AxiomDimens {
    /** Horizontal screen margin on phones. */
    val screenHorizontalPadding = 20.dp

    /** Maximum readable content width on tablets and foldables. */
    val contentMaxWidth = 720.dp

    /** Corner radius of standard glass cards. */
    val cardCornerRadius = 28.dp

    /** Corner radius of compact stat tiles. */
    val tileCornerRadius = 24.dp

    /** Corner radius of text inputs. */
    val inputCornerRadius = 20.dp

    /** Corner radius of dialogs. */
    val dialogCornerRadius = 36.dp

    /** Height of the top floating action row. */
    val topBarHeight = 56.dp

    /** Size of circular glass icon buttons (also the minimum touch target). */
    val iconButtonSize = 48.dp

    /** Height of the floating navigation bar. */
    val navigationBarHeight = 64.dp

    /** Gap between the floating navigation bar and the system navigation area. */
    val navigationBarBottomGap = 12.dp

    /** Maximum width of the floating navigation bar on large screens. */
    val navigationBarMaxWidth = 520.dp

    /** Minimum interactive target size (Material accessibility guidance). */
    val minTouchTarget = 48.dp
}
// ===== END SECTION: AXIOM_DIMENS =====
