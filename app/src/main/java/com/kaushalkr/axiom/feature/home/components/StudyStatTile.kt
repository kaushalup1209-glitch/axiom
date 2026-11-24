package com.kaushalkr.axiom.feature.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.tabularNumbers

// ===== SECTION: HOME_STUDY_STAT_TILE =====
/** Compact glass tile showing one figure (study time, distraction time). */
@Composable
fun StudyStatTile(
    @DrawableRes iconRes: Int,
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    isValueMuted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = AxiomTheme.colors
    GlassCard(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = GlassShapes.Tile,
        contentPadding = PaddingValues(AxiomSpacing.md),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
            Text(text = label, style = AxiomTheme.typography.labelLarge, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(AxiomSpacing.sm))
        // A muted value is a status ("Not tracked"), so it uses a smaller style that fits narrow tiles.
        Text(
            text = value,
            style = if (isValueMuted) AxiomTheme.typography.titleMedium else AxiomTheme.typography.headlineSmall.tabularNumbers(),
            color = if (isValueMuted) colors.textTertiary else colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.heightIn(min = 30.dp),
        )
        Text(text = caption, style = AxiomTheme.typography.bodySmall, color = colors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
// ===== END SECTION: HOME_STUDY_STAT_TILE =====
