package com.kaushalkr.axiom.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: SETTINGS_INFO_NOTE =====
/** Inline accent note explaining why a setting is limited on this device. */
@Composable
fun SettingsInfoNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedRectangle(14.dp))
            .background(colors.accentSoft)
            .padding(horizontal = AxiomSpacing.sm, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = AxiomTheme.typography.bodySmall,
            color = colors.textPrimary,
        )
    }
}
// ===== END SECTION: SETTINGS_INFO_NOTE =====
