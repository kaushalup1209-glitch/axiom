package com.kaushalkr.axiom.feature.tasks

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.domain.task.TaskActions
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate

/**
 * Task list state. Filtering, sorting and grouping run on [Dispatchers.Default]; the day
 * boundary is re-evaluated each minute only while the screen is visible.
 */
@OptIn(FlowPreview::class)
class TasksViewModel(
    repository: TaskRepository,
    private val actions: TaskActions,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    // ===== SECTION: TASKS_VM_INPUTS =====
    val searchState = TextFieldState()
    private val view = MutableStateFlow(TaskView.TODAY)
    private val filter = MutableStateFlow(TaskFilter())
    private val sort = MutableStateFlow(TaskSort.MANUAL)

    private val query = snapshotFlow { searchState.text.toString() }
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MILLIS }
        .distinctUntilChanged()

    private val today = timeProvider.ticks(MINUTE_MILLIS)
        .map { Instant.ofEpochMilli(it).atZone(timeProvider.zone()).toLocalDate() }
        .distinctUntilChanged()

    private val controls = combine(view, filter, sort, query) { view, filter, sort, query -> ListControls(view, filter, sort, query) }
    // ===== END SECTION: TASKS_VM_INPUTS =====

    // ===== SECTION: TASKS_VM_STATE =====
    val uiState: StateFlow<TasksUiState?> = combine(
        repository.openTasks,
        repository.completedTasks(),
        repository.subjects,
        today,
        controls,
    ) { open, completed, subjects, today, controls ->
        buildState(open, completed, subjects, today, controls)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initialValue = null)

    private fun buildState(
        open: List<Task>,
        completed: List<Task>,
        subjects: List<String>,
        today: LocalDate,
        controls: ListControls,
    ): TasksUiState {
        val counts = TaskListBuilder.counts(open, completed.size, today)
        val isSearching = controls.query.isNotBlank()
        return TasksUiState(
            today = today,
            view = controls.view,
            sections = TaskListBuilder.build(
                view = controls.view,
                open = open,
                completed = completed,
                today = today,
                zone = timeProvider.zone(),
                query = controls.query,
                filter = controls.filter,
                sort = controls.sort,
            ),
            counts = counts,
            dueTodayCount = counts.getValue(TaskView.TODAY),
            overdueCount = counts.getValue(TaskView.OVERDUE),
            subjects = subjects,
            filter = controls.filter,
            sort = controls.sort,
            isSearching = isSearching,
            canReorder = controls.sort == TaskSort.MANUAL && !isSearching && !controls.filter.isActive &&
                controls.view != TaskView.COMPLETED,
        )
    }
    // ===== END SECTION: TASKS_VM_STATE =====

    // ===== SECTION: TASKS_VM_ACTIONS =====
    fun selectView(newView: TaskView) {
        view.value = newView
    }

    fun setSort(newSort: TaskSort) {
        sort.value = newSort
    }

    fun togglePriorityFilter(priority: TaskPriority) {
        filter.value = filter.value.let { current ->
            current.copy(priorities = if (priority in current.priorities) current.priorities - priority else current.priorities + priority)
        }
    }

    fun setSubjectFilter(subject: String?) {
        filter.value = filter.value.copy(subject = subject)
    }

    fun clearFilters() {
        filter.value = TaskFilter()
    }

    fun toggleCompleted(item: TaskListItem) {
        if (item.isCompleted) actions.reopen(item.id) else actions.complete(item.id, item.title)
    }

    fun delete(item: TaskListItem) {
        actions.delete(item.id, item.title)
    }

    /** Persists a drag: [item] now sits between [previous] and [next] in its section. */
    fun commitMove(item: TaskListItem, previous: TaskListItem?, next: TaskListItem?) {
        actions.move(item.id, previous?.sortOrder, next?.sortOrder)
    }
    // ===== END SECTION: TASKS_VM_ACTIONS =====

    private data class ListControls(val view: TaskView, val filter: TaskFilter, val sort: TaskSort, val query: String)

    companion object {
        private const val MINUTE_MILLIS = 60_000L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val SEARCH_DEBOUNCE_MILLIS = 150L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                TasksViewModel(container.taskRepository, container.taskActions, container.timeProvider)
            }
        }
    }
}
