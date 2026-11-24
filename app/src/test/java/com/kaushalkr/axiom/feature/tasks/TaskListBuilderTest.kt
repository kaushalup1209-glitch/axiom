package com.kaushalkr.axiom.feature.tasks

import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.testing.testTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

// ===== SECTION: TEST_TASK_LIST_BUILDER =====
class TaskListBuilderTest {

    private val today = LocalDate.of(2026, 11, 24)
    private val zone = ZoneOffset.UTC

    private val open = listOf(
        testTask(1, title = "Due today", dueDate = today),
        testTask(2, title = "Overdue", dueDate = today.minusDays(2)),
        testTask(3, title = "Tomorrow", dueDate = today.plusDays(1)),
        testTask(4, title = "Later", dueDate = today.plusDays(5)),
        testTask(5, title = "Someday"),
    )

    private fun build(
        view: TaskView,
        open: List<Task> = this.open,
        completed: List<Task> = emptyList(),
        query: String = "",
        filter: TaskFilter = TaskFilter(),
        sort: TaskSort = TaskSort.MANUAL,
    ) = TaskListBuilder.build(view, open, completed, today, zone, query, filter, sort)

    private fun List<TaskSection>.ids() = flatMap { section -> section.items.map { it.id } }

    @Test
    fun viewsSplitOpenTasksByDate() {
        assertEquals(listOf(1L), build(TaskView.TODAY).ids())
        assertEquals(listOf(2L), build(TaskView.OVERDUE).ids())
        assertEquals(listOf(3L, 4L, 5L), build(TaskView.UPCOMING).ids())
        assertEquals(
            mapOf(TaskView.TODAY to 1, TaskView.UPCOMING to 3, TaskView.OVERDUE to 1, TaskView.COMPLETED to 7),
            TaskListBuilder.counts(open, completedCount = 7, today = today),
        )
    }

    @Test
    fun upcomingGroupsByDateWithNoDateLast() {
        val sections = build(TaskView.UPCOMING)
        assertEquals(listOf(TaskSectionKind.TOMORROW, TaskSectionKind.DATE, TaskSectionKind.NO_DATE), sections.map { it.kind })
        assertEquals(today.plusDays(5), sections[1].date)
    }

    @Test
    fun overdueItemsAreFlagged() {
        val item = build(TaskView.OVERDUE).single().items.single()
        assertTrue(item.isOverdue)
    }

    @Test
    fun completedGroupsByCompletionDayNewestFirst() {
        val day = 86_400_000L
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val completed = listOf(
            testTask(10, completedAtMillis = startOfToday + 1_000),
            testTask(11, completedAtMillis = startOfToday + 5_000),
            testTask(12, completedAtMillis = startOfToday - day + 1_000),
            testTask(13, completedAtMillis = startOfToday - 4 * day),
        )
        val sections = build(TaskView.COMPLETED, completed = completed)
        assertEquals(
            listOf(TaskSectionKind.COMPLETED_TODAY, TaskSectionKind.COMPLETED_YESTERDAY, TaskSectionKind.COMPLETED_DATE),
            sections.map { it.kind },
        )
        assertEquals(listOf(11L, 10L, 12L, 13L), sections.ids())
    }

    @Test
    fun searchMatchesTitleDescriptionSubjectAndSubtasks() {
        val tasks = listOf(
            testTask(1, title = "Physics revision", dueDate = today),
            testTask(2, title = "Read", description = "Chapter on OPTICS", dueDate = today),
            testTask(3, title = "Essay", subject = "Optics club", dueDate = today),
            testTask(4, title = "Lab", subtasks = listOf("Optics lab report"), dueDate = today),
            testTask(5, title = "Unrelated", dueDate = today),
        )
        assertEquals(listOf(2L, 3L, 4L), build(TaskView.TODAY, open = tasks, query = "  optics ").ids())
    }

    @Test
    fun filtersByPriorityAndSubject() {
        val tasks = listOf(
            testTask(1, priority = TaskPriority.HIGH, subject = "Maths", dueDate = today),
            testTask(2, priority = TaskPriority.LOW, subject = "Maths", dueDate = today),
            testTask(3, priority = TaskPriority.HIGH, subject = "Biology", dueDate = today),
        )
        val filter = TaskFilter(priorities = setOf(TaskPriority.HIGH), subject = "maths")
        assertEquals(listOf(1L), build(TaskView.TODAY, open = tasks, filter = filter).ids())
        assertEquals(2, filter.activeCount)
    }

    @Test
    fun sortsByPriorityAndByDeadline() {
        val tasks = listOf(
            testTask(1, priority = TaskPriority.LOW, dueDate = today.plusDays(1)),
            testTask(2, priority = TaskPriority.URGENT, dueDate = today.plusDays(3)),
            testTask(3, priority = TaskPriority.MEDIUM),
        )
        val byPriority = tasks.sortedWith(TaskListBuilder.comparatorFor(TaskSort.PRIORITY)).map { it.id }
        assertEquals(listOf(2L, 3L, 1L), byPriority)
        val byDeadline = tasks.sortedWith(TaskListBuilder.comparatorFor(TaskSort.DUE_DATE)).map { it.id }
        assertEquals(listOf(1L, 2L, 3L), byDeadline)
    }
}
// ===== END SECTION: TEST_TASK_LIST_BUILDER =====
