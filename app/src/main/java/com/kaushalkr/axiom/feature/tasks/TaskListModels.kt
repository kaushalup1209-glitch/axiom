package com.kaushalkr.axiom.feature.tasks

import androidx.compose.runtime.Immutable
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.model.TaskRecurrence
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: TASK_VIEW =====
/** The four task lists. */
enum class TaskView {
    TODAY,
    UPCOMING,
    OVERDUE,
    COMPLETED,
}

enum class TaskSort {
    MANUAL,
    DUE_DATE,
    PRIORITY,
    CREATED,
    TITLE,
}

@Immutable
data class TaskFilter(
    val priorities: Set<TaskPriority> = emptySet(),
    val subject: String? = null,
) {
    val isActive: Boolean get() = priorities.isNotEmpty() || subject != null
    val activeCount: Int get() = priorities.size + if (subject != null) 1 else 0
}
// ===== END SECTION: TASK_VIEW =====

// ===== SECTION: TASK_LIST_ITEM =====
/** Everything a task row shows. Immutable and value-equal, so unchanged rows skip recomposition. */
@Immutable
data class TaskListItem(
    val id: Long,
    val title: String,
    val subject: String,
    val priority: TaskPriority,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val isOverdue: Boolean,
    val estimatedMinutes: Int?,
    val recurrence: TaskRecurrence,
    val subtasksDone: Int,
    val subtasksTotal: Int,
    val isCompleted: Boolean,
    val sortOrder: Double,
)

enum class TaskSectionKind {
    NONE,
    TOMORROW,
    DATE,
    NO_DATE,
    COMPLETED_TODAY,
    COMPLETED_YESTERDAY,
    COMPLETED_DATE,
}

@Immutable
data class TaskSection(
    val key: String,
    val kind: TaskSectionKind,
    val date: LocalDate?,
    val items: List<TaskListItem>,
)
// ===== END SECTION: TASK_LIST_ITEM =====

// ===== SECTION: TASKS_UI_STATE =====
@Immutable
data class TasksUiState(
    val today: LocalDate,
    val view: TaskView,
    val sections: List<TaskSection>,
    val counts: Map<TaskView, Int>,
    val dueTodayCount: Int,
    val overdueCount: Int,
    val subjects: List<String>,
    val filter: TaskFilter,
    val sort: TaskSort,
    val isSearching: Boolean,
    /** Drag-to-reorder works in manual order with no search or filter. */
    val canReorder: Boolean,
) {
    val isEmpty: Boolean get() = sections.all { it.items.isEmpty() }
}
// ===== END SECTION: TASKS_UI_STATE =====
