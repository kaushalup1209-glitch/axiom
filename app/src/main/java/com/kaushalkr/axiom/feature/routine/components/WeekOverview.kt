package com.kaushalkr.axiom.feature.routine.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.feature.routine.RoutineWeekDayUi
import java.time.LocalDate

// ===== SECTION: WEEK_OVERVIEW =====
/** The week view: seven day cards in a lazy list, keyed by date. */
@Composable
fun WeekOverview(
    week: List<RoutineWeekDayUi>,
    today: LocalDate,
    onOpenDay: (LocalDate) -> Unit,
    onOpenBlock: (blockId: Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        items(items = week, key = { it.date.toEpochDay() }, contentType = { "week-day" }) { day ->
            WeekDayCard(
                day = day,
                isToday = day.date == today,
                onOpenDay = { onOpenDay(day.date) },
                onOpenBlock = onOpenBlock,
            )
        }
    }
}
// ===== END SECTION: WEEK_OVERVIEW =====
