package com.kaushalkr.axiom.feature.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomIconBadge
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.tasks.TaskView

// ===== SECTION: TASKS_EMPTY_STATE =====
@Composable
fun TasksEmptyState(
    view: TaskView,
    isFiltered: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val (titleRes, bodyRes) = when {
        isFiltered -> R.string.tasks_empty_search_title to R.string.tasks_empty_search_body
        view == TaskView.TODAY -> R.string.tasks_empty_today_title to R.string.tasks_empty_today_body
        view == TaskView.UPCOMING -> R.string.tasks_empty_upcoming_title to R.string.tasks_empty_upcoming_body
        view == TaskView.OVERDUE -> R.string.tasks_empty_overdue_title to R.string.tasks_empty_overdue_body
        else -> R.string.tasks_empty_completed_title to R.string.tasks_empty_completed_body
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AxiomSpacing.xxl, horizontal = AxiomSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        AxiomIconBadge(iconRes = if (view == TaskView.COMPLETED) R.drawable.ic_task_done else R.drawable.ic_tasks)
        Text(text = stringResource(titleRes), style = AxiomTheme.typography.titleMedium, color = colors.textPrimary, textAlign = TextAlign.Center)
        Text(text = stringResource(bodyRes), style = AxiomTheme.typography.bodyMedium, color = colors.textSecondary, textAlign = TextAlign.Center)
    }
}
// ===== END SECTION: TASKS_EMPTY_STATE =====
