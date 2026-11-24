package com.kaushalkr.axiom.core.designsystem.components

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

// ===== SECTION: AXIOM_CALENDAR_MARKS =====
/** Small dots under a day number (at most three are drawn), e.g. routine kinds and exams. */
@Immutable
data class CalendarDayMarks(
    val dots: List<Color>,
    /** Extra text for screen readers, e.g. "2 blocks, 1 exam". */
    val description: String? = null,
)
// ===== END SECTION: AXIOM_CALENDAR_MARKS =====

// ===== SECTION: AXIOM_MONTH_GRID =====
/**
 * Stateless month grid: the caller owns the visible [month]. [marks] decorates days with dots;
 * it is called once per visible day, so keep it a cheap lookup.
 */
@Composable
fun AxiomMonthGrid(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDate: LocalDate?,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    previousMonthLabel: String,
    nextMonthLabel: String,
    modifier: Modifier = Modifier,
    marks: ((LocalDate) -> CalendarDayMarks?)? = null,
) {
    val colors = AxiomTheme.colors
    // Read through the configuration so a language change recomposes.
    val locale = LocalConfiguration.current.locales[0] ?: Locale.ROOT
    val firstDayOfWeek = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "MMMMyyyy"), locale) }
    val dayFormatter = remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEEdMMMMyyyy"), locale) }

    Column(modifier = modifier.fillMaxWidth()) {
        // ===== SECTION: AXIOM_MONTH_CALENDAR_HEADER =====
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = monthFormatter.format(month),
                style = AxiomTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            GlassIconButton(
                onClick = { onMonthChange(month.minusMonths(1)) },
                iconRes = R.drawable.ic_chevron_left,
                contentDescription = previousMonthLabel,
                floating = false,
            )
            Spacer(Modifier.size(8.dp))
            GlassIconButton(
                onClick = { onMonthChange(month.plusMonths(1)) },
                iconRes = R.drawable.ic_chevron_right,
                contentDescription = nextMonthLabel,
                floating = false,
            )
        }
        // ===== END SECTION: AXIOM_MONTH_CALENDAR_HEADER =====
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().clearAndSetSemantics { }) {
            (0 until DAYS_PER_WEEK).forEach { offset ->
                Text(
                    text = firstDayOfWeek.plus(offset.toLong()).getDisplayName(TextStyle.NARROW, locale),
                    style = AxiomTheme.typography.labelMedium,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        // ===== SECTION: AXIOM_MONTH_CALENDAR_GRID =====
        val leadingBlanks = (month.atDay(1).dayOfWeek.value - firstDayOfWeek.value + DAYS_PER_WEEK) % DAYS_PER_WEEK
        val cells = leadingBlanks + month.lengthOfMonth()
        val rows = (cells + DAYS_PER_WEEK - 1) / DAYS_PER_WEEK
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(DAYS_PER_WEEK) { column ->
                        val day = row * DAYS_PER_WEEK + column - leadingBlanks + 1
                        Box(modifier = Modifier.weight(1f).height(AxiomDimens.minTouchTarget), contentAlignment = Alignment.Center) {
                            if (day in 1..month.lengthOfMonth()) {
                                val date = month.atDay(day)
                                val dayMarks = marks?.invoke(date)
                                CalendarDay(
                                    date = date,
                                    isSelected = date == selectedDate,
                                    isToday = date == today,
                                    isPast = date.isBefore(today),
                                    description = listOfNotNull(dayFormatter.format(date), dayMarks?.description).joinToString(", "),
                                    dots = dayMarks?.dots.orEmpty(),
                                    onClick = { onSelect(date) },
                                )
                            }
                        }
                    }
                }
            }
        }
        // ===== END SECTION: AXIOM_MONTH_CALENDAR_GRID =====
    }
}

private const val DAYS_PER_WEEK = 7
private const val MAX_DOTS = 3
// ===== END SECTION: AXIOM_MONTH_GRID =====

// ===== SECTION: AXIOM_CALENDAR_DAY =====
@Composable
private fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isPast: Boolean,
    description: String,
    dots: List<Color>,
    onClick: () -> Unit,
) {
    val colors = AxiomTheme.colors
    Box(
        modifier = Modifier
            .size(AxiomDimens.minTouchTarget - 6.dp)
            .clip(CircleShape)
            .background(if (isSelected) colors.accent else Color.Transparent)
            .then(if (isToday && !isSelected) Modifier.border(1.5.dp, colors.accent, CircleShape) else Modifier)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = AxiomTheme.typography.bodyLarge,
            color = when {
                isSelected -> colors.onAccent
                isToday -> colors.accent
                isPast -> colors.textTertiary
                else -> colors.textPrimary
            },
        )
        if (dots.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-4).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                dots.take(MAX_DOTS).forEach { dot ->
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.onAccent else dot),
                    )
                }
            }
        }
    }
}
// ===== END SECTION: AXIOM_CALENDAR_DAY =====
