package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomChip
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.routine.rememberMinuteFormatter
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorFieldLabel
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorSectionCard

// ===== SECTION: ROUTINE_TIME_CARD =====
/** Start, end and length. Changing the end adjusts the length; a block may run past midnight. */
@Composable
fun RoutineTimeCard(
    startMinute: Int,
    durationMinutes: Int,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onSelectDuration: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val format = rememberMinuteFormatter()
    val endMinute = startMinute + durationMinutes
    val presets = RoutineLimits.DURATION_PRESETS_MINUTES.let { list ->
        if (durationMinutes in list) list else (list + durationMinutes).sorted()
    }
    EditorSectionCard(iconRes = R.drawable.ic_study_time, title = stringResource(R.string.routine_editor_time), modifier = modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            AxiomChip(
                label = stringResource(R.string.routine_editor_starts, format(startMinute)),
                selected = true,
                onClick = onPickStart,
                iconRes = R.drawable.ic_study_time,
                role = Role.Button,
            )
            AxiomChip(
                label = stringResource(R.string.routine_editor_ends, format(endMinute)),
                selected = false,
                onClick = onPickEnd,
                role = Role.Button,
            )
        }
        if (endMinute > MINUTES_PER_DAY) {
            Text(
                text = stringResource(R.string.routine_editor_next_day),
                style = AxiomTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
        EditorFieldLabel(stringResource(R.string.routine_editor_length))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            presets.forEach { minutes ->
                AxiomChip(
                    label = formatStudyMinutes(minutes),
                    selected = minutes == durationMinutes,
                    onClick = { onSelectDuration(minutes) },
                    role = Role.RadioButton,
                )
            }
        }
    }
}
// ===== END SECTION: ROUTINE_TIME_CARD =====
