package com.kaushalkr.axiom.core.domain.task

import com.kaushalkr.axiom.core.model.TaskRecurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

// ===== SECTION: TEST_TASK_RECURRENCE =====
class TaskRecurrenceCalculatorTest {

    // Tuesday
    private val today = LocalDate.of(2026, 11, 24)

    @Test
    fun noneNeverRepeats() {
        assertNull(TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.NONE, today, today))
    }

    @Test
    fun dailyMovesToTomorrow() {
        assertEquals(today.plusDays(1), TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.DAILY, today, today))
    }

    @Test
    fun missedOccurrencesAreSkipped() {
        // Due five days ago and finished today: the next one is tomorrow, not four days ago.
        assertEquals(today.plusDays(1), TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.DAILY, today.minusDays(5), today))
        // Weekly from three weeks ago lands on the next matching weekday after today.
        assertEquals(today.plusWeeks(1), TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.WEEKLY, today.minusWeeks(3), today))
    }

    @Test
    fun completingEarlyKeepsTheSchedule() {
        val dueFriday = LocalDate.of(2026, 11, 27)
        assertEquals(dueFriday.plusWeeks(1), TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.WEEKLY, dueFriday, today))
    }

    @Test
    fun weekdaysSkipTheWeekend() {
        val friday = LocalDate.of(2026, 11, 27)
        val monday = LocalDate.of(2026, 11, 30)
        assertEquals(monday, TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.WEEKDAYS, friday, friday))
    }

    @Test
    fun monthlyClampsToShorterMonths() {
        val january31 = LocalDate.of(2027, 1, 31)
        assertEquals(LocalDate.of(2027, 2, 28), TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.MONTHLY, january31, january31))
    }

    @Test
    fun undatedTaskRepeatsFromToday() {
        assertEquals(today.plusDays(1), TaskRecurrenceCalculator.nextDueDate(TaskRecurrence.DAILY, null, today))
    }
}
// ===== END SECTION: TEST_TASK_RECURRENCE =====
