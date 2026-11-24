package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.LinkedTask
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorSectionCard

// ===== SECTION: ROUTINE_LINKED_TASKS_CARD =====
/** To-do tasks worked on in this block; their done state stays live from the task list. */
@Composable
fun RoutineLinkedTasksCard(
    linkedTasks: List<LinkedTask>,
    onLinkTasks: () -> Unit,
    onUnlink: (taskId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    EditorSectionCard(
        iconRes = R.drawable.ic_link,
        title = stringResource(R.string.routine_editor_linked_tasks),
        modifier = modifier,
        hostsGlass = true,
    ) {
        if (linkedTasks.isEmpty()) {
            Text(
                text = stringResource(R.string.routine_editor_linked_tasks_empty),
                style = AxiomTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        } else {
            Column {
                linkedTasks.forEach { task ->
                    key(task.id) { LinkedTaskRow(task = task, onUnlink = { onUnlink(task.id) }) }
                }
            }
        }
        if (linkedTasks.size >= RoutineLimits.MAX_LINKED_TASKS) {
            Text(
                text = pluralStringResource(R.plurals.routine_editor_linked_tasks_limit, RoutineLimits.MAX_LINKED_TASKS, RoutineLimits.MAX_LINKED_TASKS),
                style = AxiomTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
        GlassButton(onClick = onLinkTasks, modifier = Modifier.fillMaxWidth()) {
            Icon(painter = painterResource(R.drawable.ic_link), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(stringResource(R.string.routine_editor_link_tasks))
        }
    }
}
// ===== END SECTION: ROUTINE_LINKED_TASKS_CARD =====

// ===== SECTION: ROUTINE_LINKED_TASK_ROW =====
@Composable
private fun LinkedTaskRow(task: LinkedTask, onUnlink: () -> Unit) {
    val colors = AxiomTheme.colors
    val removeLabel = stringResource(R.string.editor_subtask_remove, task.title)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
        Icon(
            painter = painterResource(if (task.isCompleted) R.drawable.ic_task_done else R.drawable.ic_tasks),
            contentDescription = null,
            tint = if (task.isCompleted) colors.accent else colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = task.title,
            style = AxiomTheme.typography.bodyLarge.copy(
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            ),
            color = if (task.isCompleted) colors.textSecondary else colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(AxiomDimens.minTouchTarget)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = removeLabel, onClick = onUnlink),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(R.drawable.ic_close), contentDescription = removeLabel, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
        }
    }
}
// ===== END SECTION: ROUTINE_LINKED_TASK_ROW =====
