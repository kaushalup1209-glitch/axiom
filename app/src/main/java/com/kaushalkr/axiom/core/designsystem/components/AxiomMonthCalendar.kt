package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.YearMonth

// ===== SECTION: AXIOM_MONTH_CALENDAR =====
/**
 * Month grid for picking a day, sized to its container (seven equal columns), so it fits
 * narrow phones and glass sheets. Week start and names follow the UI locale. The month
 * buttons are glass and refract the panel the calendar sits on. Keeps its own visible month;
 * use [AxiomMonthGrid] to control the month from outside.
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
    var month by rememberSaveable { mutableStateOf(YearMonth.from(selectedDate ?: today)) }
    AxiomMonthGrid(
        month = month,
        onMonthChange = { month = it },
        selectedDate = selectedDate,
        today = today,
        onSelect = onSelect,
        previousMonthLabel = previousMonthLabel,
        nextMonthLabel = nextMonthLabel,
        modifier = modifier,
    )
}
// ===== END SECTION: AXIOM_MONTH_CALENDAR =====
