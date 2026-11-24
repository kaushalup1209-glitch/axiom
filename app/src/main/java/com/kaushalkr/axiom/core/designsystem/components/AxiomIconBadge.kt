package com.kaushalkr.axiom.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: AXIOM_ICON_BADGE =====
/** Decorative accent icon on a soft rounded square. */
@Composable
fun AxiomIconBadge(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val colors = AxiomTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedRectangle(size * 0.32f))
            .background(colors.accentSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}
// ===== END SECTION: AXIOM_ICON_BADGE =====
