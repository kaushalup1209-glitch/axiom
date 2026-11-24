package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.GlassToggle
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.RoutineAutomation
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorSectionCard

// ===== SECTION: ROUTINE_AUTOMATION_CARD =====
/**
 * Settings saved with the block for later phases. Each row names the phase that will use it, so
 * nothing here pretends to work yet.
 */
@Composable
fun RoutineAutomationCard(
    automation: RoutineAutomation,
    onChange: (RoutineAutomation) -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorSectionCard(
        iconRes = R.drawable.ic_shield,
        title = stringResource(R.string.routine_editor_automation),
        modifier = modifier,
        hostsGlass = true,
    ) {
        Text(
            text = stringResource(R.string.routine_editor_automation_note),
            style = AxiomTheme.typography.bodySmall,
            color = AxiomTheme.colors.textTertiary,
        )
        AutomationRow(
            title = stringResource(R.string.routine_automation_focus),
            phase = stringResource(R.string.focus_phase),
            checked = automation.autoStartFocus,
            onCheckedChange = { onChange(automation.copy(autoStartFocus = it)) },
        )
        AutomationRow(
            title = stringResource(R.string.routine_automation_track),
            phase = stringResource(R.string.routine_automation_track_phase),
            checked = automation.trackDistractions,
            onCheckedChange = { onChange(automation.copy(trackDistractions = it)) },
        )
        AutomationRow(
            title = stringResource(R.string.routine_automation_restrict),
            phase = stringResource(R.string.routine_automation_restrict_phase),
            checked = automation.restrictApps,
            onCheckedChange = { onChange(automation.copy(restrictApps = it)) },
        )
    }
}
// ===== END SECTION: ROUTINE_AUTOMATION_CARD =====

// ===== SECTION: ROUTINE_AUTOMATION_ROW =====
@Composable
private fun AutomationRow(
    title: String,
    phase: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = AxiomTheme.typography.bodyLarge, color = AxiomTheme.colors.textPrimary)
            AxiomPill(text = phase)
        }
        GlassToggle(checked = checked, onCheckedChange = onCheckedChange, contentDescription = title)
    }
}
// ===== END SECTION: ROUTINE_AUTOMATION_ROW =====
