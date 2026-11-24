package com.kaushalkr.axiom.feature.tasks.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassIconButton
import com.kaushalkr.axiom.core.designsystem.components.GlassSearchField
import com.kaushalkr.axiom.core.designsystem.components.GlassSegmentedControl
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.TaskLimits
import com.kaushalkr.axiom.feature.tasks.TaskSort
import com.kaushalkr.axiom.feature.tasks.TaskView
import com.kaushalkr.axiom.feature.tasks.labelRes

// ===== SECTION: TASKS_HEADER =====
/**
 * Fixed top of the task list: title with a New task button, a live summary, the glass search
 * field with filter and sort buttons, and the Today / Upcoming / Overdue / Done selector.
 * It stays put while the list scrolls, so no glass control sits over moving content.
 */
@Composable
fun TasksHeader(
    view: TaskView,
    dueTodayCount: Int,
    overdueCount: Int,
    activeFilterCount: Int,
    sort: TaskSort,
    searchState: TextFieldState,
    onSelectView: (TaskView) -> Unit,
    onOpenFilter: () -> Unit,
    onOpenSort: () -> Unit,
    onNewTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.tasks_eyebrow).uppercase(),
                    style = AxiomTheme.typography.labelMedium,
                    color = colors.accent,
                )
                Text(
                    text = stringResource(R.string.tasks_title),
                    style = AxiomTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
            }
            GlassButton(
                onClick = onNewTask,
                tint = colors.accent,
                contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.tasks_new))
            }
        }
        Text(
            text = summaryText(dueTodayCount, overdueCount),
            style = AxiomTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
        ) {
            GlassSearchField(
                state = searchState,
                placeholder = stringResource(R.string.tasks_search_placeholder),
                clearLabel = stringResource(R.string.action_clear_search),
                maxLength = TaskLimits.TITLE_MAX_LENGTH,
                modifier = Modifier.weight(1f),
            )
            BadgedIconButton(
                iconRes = R.drawable.ic_filter,
                contentDescription = if (activeFilterCount > 0) {
                    pluralStringResource(R.plurals.tasks_filter_active, activeFilterCount, activeFilterCount)
                } else {
                    stringResource(R.string.tasks_filter)
                },
                badge = if (activeFilterCount > 0) activeFilterCount.toString() else null,
                onClick = onOpenFilter,
            )
            BadgedIconButton(
                iconRes = R.drawable.ic_sort,
                contentDescription = stringResource(R.string.tasks_sort_current, stringResource(sort.labelRes())),
                badge = if (sort != TaskSort.MANUAL) "" else null,
                onClick = onOpenSort,
            )
        }
        GlassSegmentedControl(
            options = TaskView.entries.map { stringResource(it.labelRes()) },
            selectedIndex = view.ordinal,
            onSelect = { index -> onSelectView(TaskView.entries[index]) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun summaryText(dueTodayCount: Int, overdueCount: Int): String {
    val today = if (dueTodayCount > 0) pluralStringResource(R.plurals.tasks_summary_today, dueTodayCount, dueTodayCount) else null
    val overdue = if (overdueCount > 0) pluralStringResource(R.plurals.tasks_summary_overdue, overdueCount, overdueCount) else null
    return when {
        today != null && overdue != null -> stringResource(R.string.tasks_summary_joined, today, overdue)
        today != null -> today
        overdue != null -> overdue
        else -> stringResource(R.string.tasks_summary_clear)
    }
}
// ===== END SECTION: TASKS_HEADER =====

// ===== SECTION: TASKS_HEADER_BADGED_BUTTON =====
/** Glass icon button with an accent badge: a count, or a plain dot when [badge] is empty. */
@Composable
private fun BadgedIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    badge: String?,
    onClick: () -> Unit,
) {
    val colors = AxiomTheme.colors
    Box {
        GlassIconButton(onClick = onClick, iconRes = iconRes, contentDescription = contentDescription, floating = false)
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .defaultMinSize(minWidth = if (badge.isEmpty()) 10.dp else 18.dp, minHeight = if (badge.isEmpty()) 10.dp else 18.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                if (badge.isNotEmpty()) {
                    Text(
                        text = badge,
                        style = AxiomTheme.typography.labelSmall,
                        color = colors.onAccent,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}
// ===== END SECTION: TASKS_HEADER_BADGED_BUTTON =====
