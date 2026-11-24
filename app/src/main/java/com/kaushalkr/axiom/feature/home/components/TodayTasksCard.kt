package com.kaushalkr.axiom.feature.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.VisibilityThreshold
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomLinearProgress
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.components.GlassIconButton
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.home.HomeTaskSummary
import com.kaushalkr.axiom.feature.tasks.TaskListItem
import com.kaushalkr.axiom.feature.tasks.color
import com.kaushalkr.axiom.feature.tasks.components.TaskCompletionCheck
import com.kaushalkr.axiom.feature.tasks.dueLabel
import com.kyant.shapes.RoundedRectangle
import java.time.LocalDate
import kotlin.math.roundToInt

// ===== SECTION: HOME_TODAY_TASKS_CARD =====
/**
 * Real tasks for today: overdue first, then due today. Ticking one completes it with undo;
 * tapping its title opens the editor. Progress counts tasks finished today.
 */
@Composable
fun TodayTasksCard(
    summary: HomeTaskSummary,
    today: LocalDate,
    onToggleTask: (TaskListItem) -> Unit,
    onOpenTask: (Long) -> Unit,
    onAddTask: () -> Unit,
    onOpenPlanner: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val motion = LocalAxiomMotion.current
    GlassCard(modifier = modifier, hostsGlass = true) {
        HomeCardHeader(
            iconRes = R.drawable.ic_tasks,
            title = stringResource(R.string.home_tasks_title),
            subtitle = if (summary.overdueCount > 0) {
                pluralStringResource(R.plurals.tasks_summary_overdue, summary.overdueCount, summary.overdueCount)
            } else {
                null
            },
            trailing = {
                GlassIconButton(
                    onClick = onAddTask,
                    iconRes = R.drawable.ic_add,
                    contentDescription = stringResource(R.string.home_tasks_add),
                    floating = false,
                )
            },
        )
        Spacer(Modifier.height(AxiomSpacing.md))
        if (summary.totalCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pluralStringResource(R.plurals.home_tasks_progress, summary.totalCount, summary.doneTodayCount, summary.totalCount),
                    style = AxiomTheme.typography.labelLarge,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.home_goal_percent, (summary.progress * 100).roundToInt()),
                    style = AxiomTheme.typography.labelLarge,
                    color = colors.accent,
                )
            }
            Spacer(Modifier.height(AxiomSpacing.xs))
            AxiomLinearProgress(progress = summary.progress)
            Spacer(Modifier.height(AxiomSpacing.sm))
        }
        Column(
            modifier = Modifier.animateContentSize(motion.spring(AxiomMotionTokens.Expand, IntSize.VisibilityThreshold)),
        ) {
            if (summary.tasks.isEmpty()) {
                Text(
                    text = stringResource(if (summary.doneTodayCount > 0) R.string.home_tasks_all_done else R.string.home_tasks_empty),
                    style = AxiomTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = AxiomSpacing.xs),
                )
            } else {
                summary.tasks.forEach { task ->
                    key(task.id) {
                        HomeTaskRow(task = task, today = today, onToggle = { onToggleTask(task) }, onOpen = { onOpenTask(task.id) })
                    }
                }
            }
            if (summary.moreCount > 0) {
                Text(
                    text = pluralStringResource(R.plurals.home_tasks_more, summary.moreCount, summary.moreCount),
                    style = AxiomTheme.typography.labelLarge,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = AxiomSpacing.xs, top = AxiomSpacing.xxs),
                )
            }
        }
        Spacer(Modifier.height(AxiomSpacing.md))
        GlassButton(onClick = onOpenPlanner, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_tasks_open_planner))
            Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}
// ===== END SECTION: HOME_TODAY_TASKS_CARD =====

// ===== SECTION: HOME_TASK_ROW =====
@Composable
private fun HomeTaskRow(
    task: TaskListItem,
    today: LocalDate,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    val colors = AxiomTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TaskCompletionCheck(
            isCompleted = task.isCompleted,
            ringColor = task.priority.color(colors),
            contentDescription = stringResource(R.string.task_checkbox_complete, task.title),
            onToggle = onToggle,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedRectangle(12.dp))
                .clickable(role = Role.Button, onClick = onOpen)
                .padding(vertical = AxiomSpacing.xxs, horizontal = AxiomSpacing.xxs),
        ) {
            Text(
                text = task.title,
                style = AxiomTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val due = task.dueDate?.let { dueLabel(it, task.dueTime, today, task.isCompleted) }
            val meta = listOfNotNull(due, task.subject.takeIf { it.isNotEmpty() }).joinToString(" · ")
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = AxiomTheme.typography.bodySmall,
                    color = if (task.isOverdue) colors.danger else colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
// ===== END SECTION: HOME_TASK_ROW =====
