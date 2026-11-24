package com.kaushalkr.axiom.feature.home

import androidx.compose.runtime.Immutable
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.feature.tasks.TaskListBuilder.toItem
import com.kaushalkr.axiom.feature.tasks.TaskListItem
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: HOME_TASK_SUMMARY =====
/**
 * Real task data for the dashboard: what is due today or overdue, and how much of today's
 * work is done. "Done today" counts tasks completed today, whatever their deadline.
 */
@Immutable
data class HomeTaskSummary(
    /** Overdue tasks first, then today's, at most [HOME_TASK_LIMIT]. */
    val tasks: List<TaskListItem>,
    /** Open tasks due today or overdue that did not fit in [tasks]. */
    val moreCount: Int,
    val openTodayCount: Int,
    val overdueCount: Int,
    val doneTodayCount: Int,
) {
    val totalCount: Int get() = doneTodayCount + openTodayCount + overdueCount
    val progress: Float get() = if (totalCount == 0) 0f else doneTodayCount.toFloat() / totalCount

    companion object {
        const val HOME_TASK_LIMIT = 5

        val Empty = HomeTaskSummary(emptyList(), 0, 0, 0, 0)

        fun from(open: List<Task>, completedToday: List<Task>, today: LocalDate): HomeTaskSummary {
            val overdue = open.filter { it.dueDate?.isBefore(today) == true }
                .sortedWith(compareBy<Task> { it.dueDate }.thenByDescending { it.priority }.thenBy { it.sortOrder })
            val dueToday = open.filter { it.dueDate == today }
                .sortedWith(
                    compareBy<Task, LocalTime?>(nullsLast()) { it.dueTime }
                        .thenByDescending { it.priority }
                        .thenBy { it.sortOrder },
                )
            val relevant = overdue + dueToday
            return HomeTaskSummary(
                tasks = relevant.take(HOME_TASK_LIMIT).map { it.toItem(today) },
                moreCount = (relevant.size - HOME_TASK_LIMIT).coerceAtLeast(0),
                openTodayCount = dueToday.size,
                overdueCount = overdue.size,
                doneTodayCount = completedToday.size,
            )
        }
    }
}
// ===== END SECTION: HOME_TASK_SUMMARY =====
