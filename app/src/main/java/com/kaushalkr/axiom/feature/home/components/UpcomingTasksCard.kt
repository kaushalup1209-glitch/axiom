package com.kaushalkr.axiom.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.data.sample.PreviewTask
import com.kaushalkr.axiom.core.designsystem.components.AxiomLinearProgress
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import kotlin.math.roundToInt

// ===== SECTION: HOME_UPCOMING_TASKS_CARD =====
/** Task list with completion progress. Shows PLACEHOLDER data until Phase 03. */
@Composable
fun UpcomingTasksCard(
    tasks: List<PreviewTask>,
    completedCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val progress = if (tasks.isEmpty()) 0f else completedCount.toFloat() / tasks.size
    GlassCard(modifier = modifier) {
        HomeCardHeader(
            iconRes = R.drawable.ic_tasks,
            title = stringResource(R.string.home_tasks_title),
            trailing = { AxiomPill(text = stringResource(R.string.home_preview_badge)) },
        )
        Spacer(Modifier.height(AxiomSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(R.plurals.home_tasks_progress, tasks.size, completedCount, tasks.size),
                style = AxiomTheme.typography.labelLarge,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.home_goal_percent, (progress * 100).roundToInt()),
                style = AxiomTheme.typography.labelLarge,
                color = colors.accent,
            )
        }
        Spacer(Modifier.height(AxiomSpacing.xs))
        AxiomLinearProgress(progress = progress)
        Spacer(Modifier.height(AxiomSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            tasks.forEach { task -> TaskRow(task) }
        }
        Spacer(Modifier.height(AxiomSpacing.md))
        Text(
            text = stringResource(R.string.home_tasks_preview_note),
            style = AxiomTheme.typography.bodySmall,
            color = colors.textTertiary,
        )
    }
}
// ===== END SECTION: HOME_UPCOMING_TASKS_CARD =====

// ===== SECTION: HOME_TASK_ROW =====
@Composable
private fun TaskRow(task: PreviewTask) {
    val colors = AxiomTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Icon(
            painter = painterResource(if (task.isDone) R.drawable.ic_task_done else R.drawable.ic_task_open),
            contentDescription = stringResource(if (task.isDone) R.string.home_task_done else R.string.home_task_open),
            tint = if (task.isDone) colors.accent else colors.textTertiary,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = AxiomTheme.typography.titleSmall.copy(
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = if (task.isDone) colors.textSecondary else colors.textPrimary,
            )
            Text(text = task.subject, style = AxiomTheme.typography.bodySmall, color = colors.textTertiary)
        }
    }
}
// ===== END SECTION: HOME_TASK_ROW =====
