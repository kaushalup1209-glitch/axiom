package com.kaushalkr.axiom.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: SETTINGS_SECTION =====
/** A titled group of settings inside one glass card. */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
    ) {
        Text(
            text = title.uppercase(),
            style = AxiomTheme.typography.labelMedium,
            color = AxiomTheme.colors.textTertiary,
            modifier = Modifier
                .padding(horizontal = AxiomSpacing.xs)
                .semantics { heading() },
        )
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}
// ===== END SECTION: SETTINGS_SECTION =====

// ===== SECTION: SETTINGS_DIVIDER =====
/** Hairline separator between rows of a settings card. */
@Composable
fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = AxiomSpacing.md),
        color = AxiomTheme.colors.divider,
    )
}
// ===== END SECTION: SETTINGS_DIVIDER =====
