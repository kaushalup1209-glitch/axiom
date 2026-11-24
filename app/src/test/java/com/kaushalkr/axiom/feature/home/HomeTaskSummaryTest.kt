package com.kaushalkr.axiom.feature.home

import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.testing.testTask
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: TEST_HOME_TASK_SUMMARY =====
class HomeTaskSummaryTest {

    private val today = LocalDate.of(2026, 11, 24)

    @Test
    fun overdueComesFirstThenTodayByTime() {
        val open = listOf(
            testTask(1, dueDate = today, dueTime = LocalTime.of(18, 0)),
            testTask(2, dueDate = today, dueTime = LocalTime.of(9, 0)),
            testTask(3, dueDate = today.minusDays(1)),
            testTask(4, dueDate = today.plusDays(1)),
            testTask(5, dueDate = today, priority = TaskPriority.URGENT),
        )
        val summary = HomeTaskSummary.from(open, completedToday = emptyList(), today = today)
        assertEquals(listOf(3L, 2L, 1L, 5L), summary.tasks.map { it.id })
        assertEquals(1, summary.overdueCount)
        assertEquals(3, summary.openTodayCount)
    }

    @Test
    fun limitsTheListAndCountsTheRest() {
        val open = (1L..8L).map { testTask(it, dueDate = today) }
        val summary = HomeTaskSummary.from(open, completedToday = emptyList(), today = today)
        assertEquals(HomeTaskSummary.HOME_TASK_LIMIT, summary.tasks.size)
        assertEquals(3, summary.moreCount)
    }

    @Test
    fun progressIsDoneTodayOverAllOfTodaysWork() {
        val open = listOf(testTask(1, dueDate = today), testTask(2, dueDate = today.minusDays(3)))
        val done = listOf(testTask(3, completedAtMillis = 1L), testTask(4, completedAtMillis = 2L))
        val summary = HomeTaskSummary.from(open, done, today)
        assertEquals(4, summary.totalCount)
        assertEquals(0.5f, summary.progress, 0.0001f)
        assertEquals(0f, HomeTaskSummary.Empty.progress, 0f)
    }
}
// ===== END SECTION: TEST_HOME_TASK_SUMMARY =====
