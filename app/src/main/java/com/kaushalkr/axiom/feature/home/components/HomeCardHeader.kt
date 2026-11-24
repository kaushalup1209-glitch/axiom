package com.kaushalkr.axiom.feature.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.components.AxiomIconBadge
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: HOME_CARD_HEADER =====
/** Icon, title, optional subtitle and an optional trailing element for dashboard cards. */
@Composable
fun HomeCardHeader(
    @DrawableRes iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = AxiomTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        AxiomIconBadge(iconRes = iconRes, size = 40.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = AxiomTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            if (subtitle != null) {
                Text(text = subtitle, style = AxiomTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
        trailing?.invoke()
    }
}
// ===== END SECTION: HOME_CARD_HEADER =====
