package com.kaushalkr.axiom.feature.tasks.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.TaskRecurrence
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.tasks.TaskListItem
import com.kaushalkr.axiom.feature.tasks.color
import com.kaushalkr.axiom.feature.tasks.dueLabel
import com.kaushalkr.axiom.feature.tasks.labelRes
import java.time.LocalDate

// ===== SECTION: TASK_CARD =====
/**
 * One task row on its own Liquid Glass surface ([GlassTokens.ListItem]). Tap to edit; the
 * check completes it. Swipe and reorder are added by [SwipeableTaskItem].
 */
@Composable
fun TaskCard(
    item: TaskListItem,
    today: LocalDate,
    showDragHandle: Boolean,
    onOpen: () -> Unit,
    onToggleCompleted: () -> Unit,
    accessibilityActions: List<CustomAccessibilityAction>,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val priorityColor = item.priority.color(colors)
    val checkDescription = stringResource(
        if (item.isCompleted) R.string.task_checkbox_reopen else R.string.task_checkbox_complete,
        item.title,
    )
    GlassCard(
        modifier = modifier.semantics { customActions = accessibilityActions },
        shape = GlassShapes.Tile,
        spec = GlassTokens.ListItem,
        contentPadding = PaddingValues(start = AxiomSpacing.xxs, end = AxiomSpacing.md, top = AxiomSpacing.sm, bottom = AxiomSpacing.sm),
        onClick = onOpen,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xxs),
        ) {
            TaskCompletionCheck(
                isCompleted = item.isCompleted,
                ringColor = priorityColor,
                contentDescription = checkDescription,
                onToggle = onToggleCompleted,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.title,
                    style = AxiomTheme.typography.titleSmall.copy(
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                    color = if (item.isCompleted) colors.textSecondary else colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                TaskMetaRow(item, today)
            }
            PriorityFlag(item)
            if (showDragHandle) {
                Icon(
                    painter = painterResource(R.drawable.ic_drag),
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
// ===== END SECTION: TASK_CARD =====

// ===== SECTION: TASK_CARD_META =====
@Composable
private fun TaskMetaRow(item: TaskListItem, today: LocalDate) {
    val colors = AxiomTheme.colors
    val hasMeta = item.dueDate != null || item.subject.isNotEmpty() || item.estimatedMinutes != null ||
        item.subtasksTotal > 0 || item.recurrence != TaskRecurrence.NONE
    if (!hasMeta) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item.dueDate?.let { date ->
            MetaLabel(
                iconRes = R.drawable.ic_calendar,
                text = dueLabel(date, item.dueTime, today, item.isCompleted),
                color = if (item.isOverdue) colors.danger else colors.textSecondary,
            )
        }
        if (item.subject.isNotEmpty()) MetaLabel(R.drawable.ic_subject, item.subject, colors.textSecondary)
        item.estimatedMinutes?.let { MetaLabel(R.drawable.ic_study_time, formatStudyMinutes(it), colors.textSecondary) }
        if (item.subtasksTotal > 0) {
            MetaLabel(
                iconRes = R.drawable.ic_subtasks,
                text = stringResource(R.string.task_subtasks_progress, item.subtasksDone, item.subtasksTotal),
                color = colors.textSecondary,
            )
        }
        if (item.recurrence != TaskRecurrence.NONE) {
            MetaLabel(R.drawable.ic_repeat, stringResource(item.recurrence.labelRes()), colors.textSecondary)
        }
    }
}

@Composable
private fun MetaLabel(@DrawableRes iconRes: Int, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text = text, style = AxiomTheme.typography.bodySmall, color = color, maxLines = 1)
    }
}

@Composable
private fun PriorityFlag(item: TaskListItem) {
    val colors = AxiomTheme.colors
    Icon(
        painter = painterResource(R.drawable.ic_priority),
        contentDescription = stringResource(R.string.priority_description, stringResource(item.priority.labelRes())),
        tint = item.priority.color(colors),
        modifier = Modifier.size(18.dp),
    )
}
// ===== END SECTION: TASK_CARD_META =====
