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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

// ===== SECTION: AXIOM_MONTH_CALENDAR =====
/**
 * Month grid for picking a day, sized to its container (seven equal columns), so it fits
 * narrow phones and glass sheets. Week start and names follow the UI locale. The month
 * buttons are glass and refract the panel the calendar sits on.
 */
@Composable
fun AxiomMonthCalendar(
    selectedDate: LocalDate?,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    previousMonthLabel: String,
    nextMonthLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    // Read through the configuration so a language change recomposes.
    val locale = LocalConfiguration.current.locales[0] ?: Locale.ROOT
    var month by rememberSaveable { mutableStateOf(YearMonth.from(selectedDate ?: today)) }
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
                onClick = { month = month.minusMonths(1) },
                iconRes = R.drawable.ic_chevron_left,
                contentDescription = previousMonthLabel,
                floating = false,
            )
            Spacer(Modifier.size(8.dp))
            GlassIconButton(
                onClick = { month = month.plusMonths(1) },
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
                                CalendarDay(
                                    date = date,
                                    isSelected = date == selectedDate,
                                    isToday = date == today,
                                    isPast = date.isBefore(today),
                                    description = dayFormatter.format(date),
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
// ===== END SECTION: AXIOM_MONTH_CALENDAR =====

// ===== SECTION: AXIOM_CALENDAR_DAY =====
@Composable
private fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isPast: Boolean,
    description: String,
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
    }
}
// ===== END SECTION: AXIOM_CALENDAR_DAY =====
