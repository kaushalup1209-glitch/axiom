package com.kaushalkr.axiom.feature.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomChip
import com.kaushalkr.axiom.core.designsystem.components.GlassBottomSheet
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.feature.tasks.TaskFilter
import com.kaushalkr.axiom.feature.tasks.color
import com.kaushalkr.axiom.feature.tasks.labelRes

// ===== SECTION: TASK_FILTER_SHEET =====
/** Priority and subject filters in a Liquid Glass sheet. Changes apply immediately. */
@Composable
fun TaskFilterSheet(
    visible: Boolean,
    filter: TaskFilter,
    subjects: List<String>,
    onTogglePriority: (TaskPriority) -> Unit,
    onSelectSubject: (String?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AxiomTheme.colors
    GlassBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.tasks_filter),
        closeLabel = stringResource(R.string.sheet_close),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            Text(text = stringResource(R.string.filter_priority), style = AxiomTheme.typography.titleSmall, color = colors.textPrimary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                TaskPriority.entries.reversed().forEach { priority ->
                    AxiomChip(
                        label = stringResource(priority.labelRes()),
                        selected = priority in filter.priorities,
                        onClick = { onTogglePriority(priority) },
                        dotColor = priority.color(colors),
                    )
                }
            }
        }
        if (subjects.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
                Text(text = stringResource(R.string.filter_subject), style = AxiomTheme.typography.titleSmall, color = colors.textPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                    AxiomChip(
                        label = stringResource(R.string.filter_any_subject),
                        selected = filter.subject == null,
                        onClick = { onSelectSubject(null) },
                        role = Role.RadioButton,
                    )
                    subjects.forEach { subject ->
                        AxiomChip(
                            label = subject,
                            selected = filter.subject.equals(subject, ignoreCase = true),
                            onClick = { onSelectSubject(subject) },
                            role = Role.RadioButton,
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            GlassButton(onClick = onClear, modifier = Modifier.weight(1f), enabled = filter.isActive) {
                Text(stringResource(R.string.filter_clear))
            }
            GlassButton(onClick = onDismiss, modifier = Modifier.weight(1f), tint = colors.accent) {
                Text(stringResource(R.string.filter_done))
            }
        }
    }
}
// ===== END SECTION: TASK_FILTER_SHEET =====
