package com.kaushalkr.axiom.feature.tasks.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomTextField
import com.kaushalkr.axiom.core.designsystem.components.GlassIconButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.TaskLimits
import com.kaushalkr.axiom.feature.tasks.components.TaskCompletionCheck
import com.kaushalkr.axiom.feature.tasks.editor.EditableSubtask

// ===== SECTION: TASK_SUBTASKS_CARD =====
/** Checklist of steps. Rows are keyed, so adding or removing one leaves the others untouched. */
@Composable
fun TaskSubtasksCard(
    subtasks: List<EditableSubtask>,
    newSubtaskState: TextFieldState,
    canAdd: Boolean,
    onAdd: () -> Unit,
    onToggle: (key: Long) -> Unit,
    onRemove: (key: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    EditorSectionCard(
        iconRes = R.drawable.ic_subtasks,
        title = stringResource(R.string.editor_subtasks),
        modifier = modifier,
        hostsGlass = true,
        trailing = if (subtasks.isEmpty()) {
            null
        } else {
            {
                Text(
                    text = stringResource(R.string.task_subtasks_progress, subtasks.count { it.isDone }, subtasks.size),
                    style = AxiomTheme.typography.labelLarge,
                    color = colors.textSecondary,
                )
            }
        },
    ) {
        if (subtasks.isNotEmpty()) {
            Column {
                subtasks.forEach { subtask ->
                    key(subtask.key) {
                        SubtaskRow(subtask = subtask, onToggle = { onToggle(subtask.key) }, onRemove = { onRemove(subtask.key) })
                    }
                }
            }
        }
        if (canAdd) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
            ) {
                AxiomTextField(
                    state = newSubtaskState,
                    placeholder = stringResource(R.string.editor_subtask_placeholder),
                    label = stringResource(R.string.editor_subtask_add),
                    maxLength = TaskLimits.SUBTASK_MAX_LENGTH,
                    capitalization = KeyboardCapitalization.Sentences,
                    onImeAction = onAdd,
                    modifier = Modifier.weight(1f),
                )
                GlassIconButton(
                    onClick = onAdd,
                    iconRes = R.drawable.ic_add,
                    contentDescription = stringResource(R.string.editor_subtask_add),
                    floating = false,
                )
            }
        } else {
            Text(
                text = pluralStringResource(R.plurals.editor_subtask_limit, TaskLimits.MAX_SUBTASKS, TaskLimits.MAX_SUBTASKS),
                style = AxiomTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}
// ===== END SECTION: TASK_SUBTASKS_CARD =====

// ===== SECTION: TASK_SUBTASK_ROW =====
@Composable
private fun SubtaskRow(
    subtask: EditableSubtask,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = AxiomTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskCompletionCheck(
            isCompleted = subtask.isDone,
            ringColor = colors.textTertiary,
            contentDescription = stringResource(
                if (subtask.isDone) R.string.task_checkbox_reopen else R.string.task_checkbox_complete,
                subtask.title,
            ),
            onToggle = onToggle,
        )
        Text(
            text = subtask.title,
            style = AxiomTheme.typography.bodyLarge.copy(
                textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None,
            ),
            color = if (subtask.isDone) colors.textSecondary else colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        val removeLabel = stringResource(R.string.editor_subtask_remove, subtask.title)
        Box(
            modifier = Modifier
                .size(AxiomDimens.minTouchTarget)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = removeLabel, onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = removeLabel,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
// ===== END SECTION: TASK_SUBTASK_ROW =====
