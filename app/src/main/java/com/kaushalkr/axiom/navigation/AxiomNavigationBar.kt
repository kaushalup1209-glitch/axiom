package com.kaushalkr.axiom.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.components.GlassTab
import com.kaushalkr.axiom.core.designsystem.components.GlassTabRow
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: NAV_FLOATING_GLASS_BAR =====
/**
 * Floating Liquid Glass navigation bar for the five primary tabs. The selection indicator
 * can be dragged between tabs; it refracts the screen content scrolling underneath.
 */
@Composable
fun AxiomNavigationBar(
    selectedDestination: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val destinations = TopLevelDestination.entries
    val contentColor = AxiomTheme.colors.textPrimary
    val iconColorFilter = ColorFilter.tint(contentColor)

    GlassTabRow(
        selectedIndex = selectedDestination.ordinal,
        onTabSelected = { index -> onDestinationSelected(destinations[index]) },
        tabsCount = destinations.size,
        modifier = modifier,
        containerHeight = AxiomDimens.navigationBarHeight,
        indicatorHeight = AxiomDimens.navigationBarHeight - 8.dp,
        floating = true,
    ) {
        destinations.forEach { destination ->
            GlassTab(
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
            ) {
                Box(
                    Modifier
                        .size(24.dp)
                        .paint(painterResource(destination.iconRes), colorFilter = iconColorFilter),
                )
                Text(
                    text = stringResource(destination.labelRes),
                    style = AxiomTheme.typography.labelSmall,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
// ===== END SECTION: NAV_FLOATING_GLASS_BAR =====
