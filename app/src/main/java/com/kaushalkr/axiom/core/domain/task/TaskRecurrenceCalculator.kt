package com.kaushalkr.axiom.core.domain.task

import com.kaushalkr.axiom.core.model.TaskRecurrence
import java.time.DayOfWeek
import java.time.LocalDate

// ===== SECTION: DOMAIN_TASK_RECURRENCE =====
/** Pure recurrence rules, unit tested without Android. */
object TaskRecurrenceCalculator {

    /**
     * The next due date after completing an occurrence due on [dueDate] (or today when the task
     * had no date). Missed occurrences are skipped: the result is always after [today].
     * Returns null for [TaskRecurrence.NONE].
     */
    fun nextDueDate(recurrence: TaskRecurrence, dueDate: LocalDate?, today: LocalDate): LocalDate? {
        if (recurrence == TaskRecurrence.NONE) return null
        var next = advance(recurrence, dueDate ?: today)
        while (!next.isAfter(today)) {
            next = advance(recurrence, next)
        }
        return next
    }

    private fun advance(recurrence: TaskRecurrence, from: LocalDate): LocalDate = when (recurrence) {
        TaskRecurrence.NONE -> from
        TaskRecurrence.DAILY -> from.plusDays(1)
        TaskRecurrence.WEEKDAYS -> {
            var day = from.plusDays(1)
            while (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) day = day.plusDays(1)
            day
        }
        TaskRecurrence.WEEKLY -> from.plusWeeks(1)
        TaskRecurrence.MONTHLY -> from.plusMonths(1)
    }
}
// ===== END SECTION: DOMAIN_TASK_RECURRENCE =====
