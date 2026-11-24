package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassBottomSheet
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.feature.tasks.color
import com.kaushalkr.axiom.feature.tasks.components.TaskCompletionCheck
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: TASK_LINK_SHEET =====
/** Picks open to-do tasks to link with a routine block. Changes apply as they are ticked. */
@Composable
fun TaskLinkSheet(
    visible: Boolean,
    openTasks: List<Task>,
    linkedIds: Set<Long>,
    onToggle: (Task) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AxiomTheme.colors
    GlassBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.routine_editor_link_tasks),
        closeLabel = stringResource(R.string.sheet_close),
    ) {
        if (openTasks.isEmpty()) {
            Text(
                text = stringResource(R.string.routine_link_sheet_empty),
                style = AxiomTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        } else {
            Column {
                openTasks.take(MAX_LISTED_TASKS).forEach { task ->
                    key(task.id) {
                        val linked = task.id in linkedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RowShape)
                                .toggleable(value = linked, role = Role.Checkbox, onValueChange = { onToggle(task) })
                                .defaultMinSize(minHeight = AxiomDimens.minTouchTarget)
                                .padding(end = AxiomSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TaskCompletionCheck(
                                isCompleted = linked,
                                ringColor = task.priority.color(colors),
                                contentDescription = task.title,
                                onToggle = { onToggle(task) },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, style = AxiomTheme.typography.bodyLarge, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (task.subject.isNotEmpty()) {
                                    Text(task.subject, style = AxiomTheme.typography.bodySmall, color = colors.textTertiary, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
        GlassButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), tint = colors.accent) {
            Text(stringResource(R.string.filter_done))
        }
    }
}

private val RowShape = RoundedRectangle(14.dp)
private const val MAX_LISTED_TASKS = 100
// ===== END SECTION: TASK_LINK_SHEET =====
