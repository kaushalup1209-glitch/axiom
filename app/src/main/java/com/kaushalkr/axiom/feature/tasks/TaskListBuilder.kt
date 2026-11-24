package com.kaushalkr.axiom.feature.tasks

import com.kaushalkr.axiom.core.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// ===== SECTION: TASK_LIST_BUILDER =====
/**
 * Pure rules that turn stored tasks into the four lists: Today (due today), Upcoming (due later
 * or no date), Overdue (due before today) and Completed (newest first, grouped by day).
 * Kept free of Android so it is unit tested directly and can run off the main thread.
 */
object TaskListBuilder {

    fun counts(open: List<Task>, completedCount: Int, today: LocalDate): Map<TaskView, Int> = mapOf(
        TaskView.TODAY to open.count { it.dueDate == today },
        TaskView.UPCOMING to open.count { it.dueDate == null || it.dueDate.isAfter(today) },
        TaskView.OVERDUE to open.count { it.dueDate?.isBefore(today) == true },
        TaskView.COMPLETED to completedCount,
    )

    fun build(
        view: TaskView,
        open: List<Task>,
        completed: List<Task>,
        today: LocalDate,
        zone: ZoneId,
        query: String,
        filter: TaskFilter,
        sort: TaskSort,
    ): List<TaskSection> {
        val matching: (Task) -> Boolean = { matches(it, query.trim(), filter) }
        val comparator = comparatorFor(sort)
        return when (view) {
            TaskView.TODAY -> single("today", open.filter { it.dueDate == today && matching(it) }, comparator, today)
            TaskView.OVERDUE -> single("overdue", open.filter { it.dueDate?.isBefore(today) == true && matching(it) }, comparator, today)
            TaskView.UPCOMING -> upcoming(open.filter(matching), today, comparator)
            TaskView.COMPLETED -> completed(completed.filter(matching), today, zone)
        }
    }

    // ===== SECTION: TASK_LIST_BUILDER_MATCHING =====
    fun matches(task: Task, query: String, filter: TaskFilter): Boolean {
        if (filter.priorities.isNotEmpty() && task.priority !in filter.priorities) return false
        if (filter.subject != null && !task.subject.equals(filter.subject, ignoreCase = true)) return false
        if (query.isEmpty()) return true
        return task.title.contains(query, ignoreCase = true) ||
            task.description.contains(query, ignoreCase = true) ||
            task.subject.contains(query, ignoreCase = true) ||
            task.subtasks.any { it.title.contains(query, ignoreCase = true) }
    }

    fun comparatorFor(sort: TaskSort): Comparator<Task> = when (sort) {
        TaskSort.MANUAL -> compareBy { it.sortOrder }
        TaskSort.DUE_DATE -> compareBy<Task, Long?>(nullsLast()) { it.dueDate?.toEpochDay() }
            .thenBy(nullsLast()) { it.dueTime }
            .thenByDescending { it.priority }
            .thenBy { it.sortOrder }
        TaskSort.PRIORITY -> compareByDescending<Task> { it.priority }
            .thenBy(nullsLast()) { it.dueDate?.toEpochDay() }
            .thenBy { it.sortOrder }
        TaskSort.CREATED -> compareByDescending { it.createdAtMillis }
        TaskSort.TITLE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    }
    // ===== END SECTION: TASK_LIST_BUILDER_MATCHING =====

    // ===== SECTION: TASK_LIST_BUILDER_SECTIONS =====
    private fun single(key: String, tasks: List<Task>, comparator: Comparator<Task>, today: LocalDate): List<TaskSection> =
        listOf(TaskSection(key, TaskSectionKind.NONE, null, tasks.sortedWith(comparator).map { it.toItem(today) }))

    private fun upcoming(tasks: List<Task>, today: LocalDate, comparator: Comparator<Task>): List<TaskSection> {
        val dated = tasks.filter { it.dueDate?.isAfter(today) == true }
            .groupBy { it.dueDate!! }
            .toSortedMap()
            .map { (date, dayTasks) ->
                TaskSection(
                    key = "date-${date.toEpochDay()}",
                    kind = if (date == today.plusDays(1)) TaskSectionKind.TOMORROW else TaskSectionKind.DATE,
                    date = date,
                    items = dayTasks.sortedWith(comparator).map { it.toItem(today) },
                )
            }
        val undated = tasks.filter { it.dueDate == null }
        val noDateSection = if (undated.isEmpty()) {
            emptyList()
        } else {
            listOf(TaskSection("no-date", TaskSectionKind.NO_DATE, null, undated.sortedWith(comparator).map { it.toItem(today) }))
        }
        return dated + noDateSection
    }

    private fun completed(tasks: List<Task>, today: LocalDate, zone: ZoneId): List<TaskSection> =
        tasks.groupBy { Instant.ofEpochMilli(it.completedAtMillis ?: 0L).atZone(zone).toLocalDate() }
            .toSortedMap(compareByDescending { it })
            .map { (date, dayTasks) ->
                TaskSection(
                    key = "done-${date.toEpochDay()}",
                    kind = when (date) {
                        today -> TaskSectionKind.COMPLETED_TODAY
                        today.minusDays(1) -> TaskSectionKind.COMPLETED_YESTERDAY
                        else -> TaskSectionKind.COMPLETED_DATE
                    },
                    date = date,
                    items = dayTasks.sortedByDescending { it.completedAtMillis }.map { it.toItem(today) },
                )
            }

    fun Task.toItem(today: LocalDate): TaskListItem = TaskListItem(
        id = id,
        title = title,
        subject = subject,
        priority = priority,
        dueDate = dueDate,
        dueTime = dueTime,
        isOverdue = !isCompleted && dueDate?.isBefore(today) == true,
        estimatedMinutes = estimatedMinutes,
        recurrence = recurrence,
        subtasksDone = completedSubtasks,
        subtasksTotal = subtasks.size,
        isCompleted = isCompleted,
        sortOrder = sortOrder,
    )
    // ===== END SECTION: TASK_LIST_BUILDER_SECTIONS =====
}
// ===== END SECTION: TASK_LIST_BUILDER =====
