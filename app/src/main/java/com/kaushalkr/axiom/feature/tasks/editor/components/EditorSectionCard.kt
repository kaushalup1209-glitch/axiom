package com.kaushalkr.axiom.feature.tasks.editor.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: EDITOR_SECTION_CARD =====
/**
 * Glass card grouping related editor fields under an icon and heading.
 *
 * @param hostsGlass true when the card holds glass controls, so they refract the card.
 * @param trailing optional element at the end of the heading row (e.g. a count).
 */
@Composable
fun EditorSectionCard(
    @DrawableRes iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    hostsGlass: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AxiomTheme.colors
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        hostsGlass = hostsGlass,
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            Text(
                text = title,
                style = AxiomTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            trailing?.invoke()
        }
        content()
    }
}
// ===== END SECTION: EDITOR_SECTION_CARD =====
