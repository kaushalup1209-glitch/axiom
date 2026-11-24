package com.kaushalkr.axiom.feature.routine

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.theme.AxiomColors
import com.kaushalkr.axiom.core.designsystem.theme.RoutinePalette
import com.kaushalkr.axiom.core.designsystem.theme.SubjectPalette
import com.kaushalkr.axiom.core.model.ExamKind
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineBlock
import com.kaushalkr.axiom.core.model.RoutineKind
import com.kaushalkr.axiom.core.model.Weekdays
import com.kaushalkr.axiom.feature.home.currentUiLocale
import com.kaushalkr.axiom.feature.home.formatTime
import com.kaushalkr.axiom.feature.home.rememberTimeFormatter
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.temporal.WeekFields

// ===== SECTION: ROUTINE_FORMAT_KINDS =====
@StringRes
fun RoutineKind.labelRes(): Int = when (this) {
    RoutineKind.STUDY -> R.string.routine_kind_study
    RoutineKind.BREAK -> R.string.routine_kind_break
    RoutineKind.SLEEP -> R.string.routine_kind_sleep
    RoutineKind.PERSONAL -> R.string.routine_kind_personal
}

/** Used as the title when a block is saved without one. */
@StringRes
fun RoutineKind.defaultTitleRes(): Int = when (this) {
    RoutineKind.STUDY -> R.string.routine_default_study
    RoutineKind.BREAK -> R.string.routine_default_break
    RoutineKind.SLEEP -> R.string.routine_default_sleep
    RoutineKind.PERSONAL -> R.string.routine_default_personal
}

@DrawableRes
fun RoutineKind.iconRes(): Int = when (this) {
    RoutineKind.STUDY -> R.drawable.ic_school
    RoutineKind.BREAK -> R.drawable.ic_break
    RoutineKind.SLEEP -> R.drawable.ic_sleep
    RoutineKind.PERSONAL -> R.drawable.ic_personal
}

/** Study blocks with a subject take the subject colour; everything else the kind colour. */
fun RoutineBlock.color(colors: AxiomColors): Color {
    val subject = subject
    return if (kind == RoutineKind.STUDY && subject != null) {
        SubjectPalette.color(subject.colorIndex, colors.isDark)
    } else {
        RoutinePalette.color(kind, colors)
    }
}

@StringRes
fun ExamKind.labelRes(): Int = when (this) {
    ExamKind.EXAM -> R.string.exam_kind_exam
    ExamKind.TEST -> R.string.exam_kind_test
    ExamKind.ASSIGNMENT -> R.string.exam_kind_assignment
    ExamKind.PROJECT -> R.string.exam_kind_project
    ExamKind.DEADLINE -> R.string.exam_kind_deadline
}
// ===== END SECTION: ROUTINE_FORMAT_KINDS =====

// ===== SECTION: ROUTINE_FORMAT_TIME =====
/** Localized time for a minute of the day ("07:30" or "7:30 AM"); wraps past midnight. */
@Composable
fun rememberMinuteFormatter(): (Int) -> String {
    val formatter = rememberTimeFormatter()
    return remember(formatter) {
        { minute: Int ->
            val wrapped = minute.mod(MINUTES_PER_DAY)
            formatter.formatTime(LocalTime.of(wrapped / 60, wrapped % 60))
        }
    }
}

/** "07:00 – 07:45". */
@Composable
fun timeRangeText(startMinute: Int, endMinute: Int): String {
    val format = rememberMinuteFormatter()
    return stringResource(R.string.routine_time_range, format(startMinute), format(endMinute))
}

/** Countdown to a date: "Today", "Tomorrow", "In 5 days", "2 days ago". */
@Composable
fun countdownText(daysUntil: Long): String = when {
    daysUntil == 0L -> stringResource(R.string.view_today)
    daysUntil == 1L -> stringResource(R.string.section_tomorrow)
    daysUntil > 1L -> pluralStringResource(R.plurals.exam_in_days, daysUntil.toInt(), daysUntil.toInt())
    else -> pluralStringResource(R.plurals.exam_days_ago, (-daysUntil).toInt(), (-daysUntil).toInt())
}
// ===== END SECTION: ROUTINE_FORMAT_TIME =====

// ===== SECTION: ROUTINE_FORMAT_DAYS =====
/** The seven weekdays in the locale's order (Monday first in most of the world). */
@Composable
fun rememberWeekdayOrder(): List<DayOfWeek> {
    val locale = currentUiLocale()
    return remember(locale) {
        val first = WeekFields.of(locale).firstDayOfWeek
        (0L until 7L).map { first.plus(it) }
    }
}

@Composable
fun weekdayShortName(day: DayOfWeek): String {
    val locale = currentUiLocale()
    return remember(day, locale) { day.getDisplayName(TextStyle.SHORT, locale) }
}

/** "Every day", "Weekdays", "Weekends" or "Mon, Wed, Fri". */
@Composable
fun repeatSummary(daysMask: Int): String {
    val locale = currentUiLocale()
    val order = rememberWeekdayOrder()
    return when (daysMask) {
        Weekdays.EVERY_DAY -> stringResource(R.string.routine_repeat_every_day)
        Weekdays.MONDAY_TO_FRIDAY -> stringResource(R.string.routine_repeat_weekdays)
        Weekdays.WEEKEND -> stringResource(R.string.routine_repeat_weekends)
        else -> remember(daysMask, locale, order) {
            order.filter { Weekdays.contains(daysMask, it) }.joinToString(", ") { it.getDisplayName(TextStyle.SHORT, locale) }
        }
    }
}
// ===== END SECTION: ROUTINE_FORMAT_DAYS =====
