package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: GLASS_SEGMENTED_CONTROL =====
/**
 * Single-choice text selector built on [GlassTabRow], so it shares the draggable,
 * refracting Liquid Glass indicator with the navigation bar.
 */
@Composable
fun GlassSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    GlassTabRow(
        selectedIndex = selectedIndex,
        onTabSelected = onSelect,
        tabsCount = options.size,
        modifier = modifier,
        containerHeight = 48.dp,
        indicatorHeight = 40.dp,
        containerColor = colors.controlTrack,
        indicatorColor = colors.segmentIndicator,
        floating = false,
        spec = GlassTokens.SegmentedControl,
    ) {
        options.forEachIndexed { index, label ->
            GlassTab(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            ) {
                Text(
                    text = label,
                    style = AxiomTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
// ===== END SECTION: GLASS_SEGMENTED_CONTROL =====
