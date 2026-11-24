package com.kaushalkr.axiom.feature.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.feature.tasks.components.TaskFilterSheet
import com.kaushalkr.axiom.feature.tasks.components.TaskList
import com.kaushalkr.axiom.feature.tasks.components.TaskSortSheet
import com.kaushalkr.axiom.feature.tasks.components.TasksHeader

// ===== SECTION: TASK_LIST_ROUTE =====
/**
 * Stateful entry point for the task list.
 *
 * @param onNewTask opens the editor; `dueToday` is true when the user starts from Today.
 */
@Composable
fun TaskListRoute(
    onOpenTask: (taskId: Long) -> Unit,
    onNewTask: (dueToday: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilter by rememberSaveable { mutableStateOf(false) }
    var showSort by rememberSaveable { mutableStateOf(false) }

    TasksScreen(
        state = state,
        searchState = viewModel.searchState,
        onSelectView = viewModel::selectView,
        onOpenFilter = { showFilter = true },
        onOpenSort = { showSort = true },
        onNewTask = { onNewTask((state?.view ?: TaskView.TODAY) == TaskView.TODAY) },
        onOpenTask = onOpenTask,
        onToggleCompleted = viewModel::toggleCompleted,
        onDelete = viewModel::delete,
        onCommitMove = viewModel::commitMove,
        modifier = modifier,
    )

    val loaded = state ?: return
    TaskFilterSheet(
        visible = showFilter,
        filter = loaded.filter,
        subjects = loaded.subjects,
        onTogglePriority = viewModel::togglePriorityFilter,
        onSelectSubject = viewModel::setSubjectFilter,
        onClear = viewModel::clearFilters,
        onDismiss = { showFilter = false },
    )
    TaskSortSheet(
        visible = showSort,
        sort = loaded.sort,
        onSelect = viewModel::setSort,
        onDismiss = { showSort = false },
    )
}
// ===== END SECTION: TASK_LIST_ROUTE =====

// ===== SECTION: TASKS_SCREEN =====
/**
 * Task list screen: a fixed glass header above a lazy list. The list's bottom padding clears
 * the floating navigation bar; on tablets the content is centred at the readable width.
 */
@Composable
fun TasksScreen(
    state: TasksUiState?,
    searchState: TextFieldState,
    onSelectView: (TaskView) -> Unit,
    onOpenFilter: () -> Unit,
    onOpenSort: () -> Unit,
    onNewTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onToggleCompleted: (TaskListItem) -> Unit,
    onDelete: (TaskListItem) -> Unit,
    onCommitMove: (item: TaskListItem, previous: TaskListItem?, next: TaskListItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chromePadding = LocalScreenContentPadding.current
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = AxiomDimens.contentMaxWidth)
                .fillMaxSize()
                .padding(
                    start = chromePadding.calculateStartPadding(layoutDirection),
                    end = chromePadding.calculateEndPadding(layoutDirection),
                    top = chromePadding.calculateTopPadding(),
                ),
        ) {
            TasksHeader(
                view = state?.view ?: TaskView.TODAY,
                dueTodayCount = state?.dueTodayCount ?: 0,
                overdueCount = state?.overdueCount ?: 0,
                activeFilterCount = state?.filter?.activeCount ?: 0,
                sort = state?.sort ?: TaskSort.MANUAL,
                searchState = searchState,
                onSelectView = onSelectView,
                onOpenFilter = onOpenFilter,
                onOpenSort = onOpenSort,
                onNewTask = onNewTask,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AxiomDimens.screenHorizontalPadding),
            )
            if (state != null) {
                TaskList(
                    state = state,
                    onOpenTask = onOpenTask,
                    onToggleCompleted = onToggleCompleted,
                    onDelete = onDelete,
                    onCommitMove = onCommitMove,
                    contentPadding = PaddingValues(
                        start = AxiomDimens.screenHorizontalPadding,
                        end = AxiomDimens.screenHorizontalPadding,
                        top = AxiomSpacing.md,
                        bottom = chromePadding.calculateBottomPadding(),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}
// ===== END SECTION: TASKS_SCREEN =====
