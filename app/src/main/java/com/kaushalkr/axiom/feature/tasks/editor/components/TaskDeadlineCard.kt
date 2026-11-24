package com.kaushalkr.axiom.feature.tasks.editor.components

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
import com.kaushalkr.axiom.core.model.TaskRecurrence
import com.kaushalkr.axiom.feature.home.formatTime
import com.kaushalkr.axiom.feature.home.rememberTimeFormatter
import com.kaushalkr.axiom.feature.tasks.labelRes
import com.kaushalkr.axiom.feature.tasks.rememberShortDateFormatter
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: TASK_DEADLINE_CARD =====
/** Deadline, time and repeat. Quick choices cover most cases; the calendar handles the rest. */
@Composable
fun TaskDeadlineCard(
    dueDate: LocalDate?,
    dueTime: LocalTime?,
    recurrence: TaskRecurrence,
    today: LocalDate,
    onSetDate: (LocalDate?) -> Unit,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onClearTime: () -> Unit,
    onSelectRecurrence: (TaskRecurrence) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tomorrow = today.plusDays(1)
    val nextWeek = today.plusWeeks(1)
    val isQuickDate = dueDate == null || dueDate == today || dueDate == tomorrow || dueDate == nextWeek
    val dateFormatter = rememberShortDateFormatter()
    val timeFormatter = rememberTimeFormatter()

    EditorSectionCard(iconRes = R.drawable.ic_calendar, title = stringResource(R.string.editor_deadline), modifier = modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            AxiomChip(stringResource(R.string.editor_date_none), selected = dueDate == null, onClick = { onSetDate(null) }, role = Role.RadioButton)
            AxiomChip(stringResource(R.string.view_today), selected = dueDate == today, onClick = { onSetDate(today) }, role = Role.RadioButton)
            AxiomChip(stringResource(R.string.section_tomorrow), selected = dueDate == tomorrow, onClick = { onSetDate(tomorrow) }, role = Role.RadioButton)
            AxiomChip(stringResource(R.string.editor_date_next_week), selected = dueDate == nextWeek, onClick = { onSetDate(nextWeek) }, role = Role.RadioButton)
            AxiomChip(
                label = dueDate?.takeIf { !isQuickDate }?.let(dateFormatter::format) ?: stringResource(R.string.editor_date_pick),
                selected = !isQuickDate,
                onClick = onPickDate,
                iconRes = R.drawable.ic_calendar,
                role = Role.Button,
            )
        }
        if (dueDate != null) {
            EditorFieldLabel(stringResource(R.string.editor_time))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                AxiomChip(stringResource(R.string.editor_time_any), selected = dueTime == null, onClick = onClearTime, role = Role.RadioButton)
                AxiomChip(
                    label = dueTime?.let { timeFormatter.formatTime(it) } ?: stringResource(R.string.editor_time_pick),
                    selected = dueTime != null,
                    onClick = onPickTime,
                    iconRes = R.drawable.ic_study_time,
                    role = Role.Button,
                )
            }
        }
        EditorFieldLabel(stringResource(R.string.editor_repeat))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            TaskRecurrence.entries.forEach { option ->
                AxiomChip(
                    label = stringResource(option.labelRes()),
                    selected = option == recurrence,
                    onClick = { onSelectRecurrence(option) },
                    iconRes = if (option != TaskRecurrence.NONE && option == recurrence) R.drawable.ic_repeat else null,
                    role = Role.RadioButton,
                )
            }
        }
        if (recurrence != TaskRecurrence.NONE) {
            Text(
                text = stringResource(R.string.editor_repeat_hint),
                style = AxiomTheme.typography.bodySmall,
                color = AxiomTheme.colors.textTertiary,
            )
        }
    }
}
// ===== END SECTION: TASK_DEADLINE_CARD =====
