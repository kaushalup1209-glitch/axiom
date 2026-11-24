package com.kaushalkr.axiom.feature.settings.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: SETTINGS_ACTION_ROW =====
/** Tappable settings row that opens a detail or confirmation. */
@Composable
fun SettingsActionRow(
    @DrawableRes iconRes: Int,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    val colors = AxiomTheme.colors
    val titleColor = if (isDestructive) colors.danger else colors.textPrimary
    val iconColor = if (isDestructive) colors.danger else colors.textSecondary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedRectangle(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = AxiomDimens.minTouchTarget)
            .padding(vertical = AxiomSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = title, style = AxiomTheme.typography.titleSmall, color = titleColor)
            Text(text = description, style = AxiomTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}
// ===== END SECTION: SETTINGS_ACTION_ROW =====
