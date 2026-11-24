/*
 * Adapted from Kyant0/AndroidLiquidGlass:
 *   app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidBottomTab.kt
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0 (see THIRD_PARTY_NOTICES.md).
 * Changes for Axiom: selected-state semantics, content description, and section labels.
 */
package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule

// ===== SECTION: GLASS_TAB_SCALE_LOCAL =====
/** Scale applied to tinted tab content while the indicator is pressed. */
internal val LocalGlassTabScale = staticCompositionLocalOf { { 1f } }
// ===== END SECTION: GLASS_TAB_SCALE_LOCAL =====

// ===== SECTION: GLASS_TAB =====
/** One equally weighted tab inside a [GlassTabRow]. */
@Composable
fun RowScope.GlassTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scale = LocalGlassTabScale.current
    Column(
        modifier
            .clip(Capsule())
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                val currentScale = scale()
                scaleX = currentScale
                scaleY = currentScale
            },
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}
// ===== END SECTION: GLASS_TAB =====
