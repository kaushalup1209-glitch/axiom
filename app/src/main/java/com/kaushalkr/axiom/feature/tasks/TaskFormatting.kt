package com.kaushalkr.axiom.feature.tasks

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.theme.AxiomColors
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.model.TaskRecurrence
import com.kaushalkr.axiom.feature.home.currentUiLocale
import com.kaushalkr.axiom.feature.home.formatTime
import com.kaushalkr.axiom.feature.home.rememberTimeFormatter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ===== SECTION: TASK_FORMAT_LABELS =====
@StringRes
fun TaskPriority.labelRes(): Int = when (this) {
    TaskPriority.LOW -> R.string.priority_low
    TaskPriority.MEDIUM -> R.string.priority_medium
    TaskPriority.HIGH -> R.string.priority_high
    TaskPriority.URGENT -> R.string.priority_urgent
}

fun TaskPriority.color(colors: AxiomColors): Color = when (this) {
    TaskPriority.LOW -> colors.priorityLow
    TaskPriority.MEDIUM -> colors.priorityMedium
    TaskPriority.HIGH -> colors.priorityHigh
    TaskPriority.URGENT -> colors.priorityUrgent
}

@StringRes
fun TaskRecurrence.labelRes(): Int = when (this) {
    TaskRecurrence.NONE -> R.string.recurrence_none
    TaskRecurrence.DAILY -> R.string.recurrence_daily
    TaskRecurrence.WEEKDAYS -> R.string.recurrence_weekdays
    TaskRecurrence.WEEKLY -> R.string.recurrence_weekly
    TaskRecurrence.MONTHLY -> R.string.recurrence_monthly
}

@StringRes
fun TaskView.labelRes(): Int = when (this) {
    TaskView.TODAY -> R.string.view_today
    TaskView.UPCOMING -> R.string.view_upcoming
    TaskView.OVERDUE -> R.string.view_overdue
    TaskView.COMPLETED -> R.string.view_completed
}

@StringRes
fun TaskSort.labelRes(): Int = when (this) {
    TaskSort.MANUAL -> R.string.sort_manual
    TaskSort.DUE_DATE -> R.string.sort_due
    TaskSort.PRIORITY -> R.string.sort_priority
    TaskSort.CREATED -> R.string.sort_created
    TaskSort.TITLE -> R.string.sort_title
}
// ===== END SECTION: TASK_FORMAT_LABELS =====

// ===== SECTION: TASK_FORMAT_DATES =====
/** Localized short date, e.g. "Wed, 26 Nov". */
@Composable
fun rememberShortDateFormatter(): DateTimeFormatter {
    val locale = currentUiLocale()
    return remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEdMMM"), locale) }
}

/** Localized long date for section headings, e.g. "Wednesday, 26 November". */
@Composable
fun rememberLongDateFormatter(): DateTimeFormatter {
    val locale = currentUiLocale()
    return remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM"), locale) }
}

/** Deadline label relative to [today]: "Today, 16:30", "Tomorrow", "3 days overdue", "Wed, 26 Nov". */
@Composable
fun dueLabel(date: LocalDate, time: LocalTime?, today: LocalDate, isCompleted: Boolean): String {
    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = rememberShortDateFormatter()
    val timeText = time?.let { timeFormatter.formatTime(it) }
    return when {
        !isCompleted && date.isBefore(today) -> {
            val days = ChronoUnit.DAYS.between(date, today).toInt()
            pluralStringResource(R.plurals.task_overdue_days, days, days)
        }
        date == today -> if (timeText != null) stringResource(R.string.task_due_today_at, timeText) else stringResource(R.string.view_today)
        date == today.plusDays(1) ->
            if (timeText != null) stringResource(R.string.task_due_tomorrow_at, timeText) else stringResource(R.string.section_tomorrow)
        else -> {
            val dateText = dateFormatter.format(date)
            if (timeText != null) stringResource(R.string.task_due_date_at, dateText, timeText) else dateText
        }
    }
}

@Composable
@ReadOnlyComposable
fun sectionTitle(section: TaskSection, longDateFormatter: DateTimeFormatter): String? = when (section.kind) {
    TaskSectionKind.NONE -> null
    TaskSectionKind.TOMORROW -> stringResource(R.string.section_tomorrow)
    TaskSectionKind.NO_DATE -> stringResource(R.string.section_no_date)
    TaskSectionKind.COMPLETED_TODAY -> stringResource(R.string.view_today)
    TaskSectionKind.COMPLETED_YESTERDAY -> stringResource(R.string.section_yesterday)
    TaskSectionKind.DATE, TaskSectionKind.COMPLETED_DATE -> section.date?.let(longDateFormatter::format)
}
// ===== END SECTION: TASK_FORMAT_DATES =====
