package com.kaushalkr.axiom.feature.routine.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomMonthGrid
import com.kaushalkr.axiom.core.designsystem.components.CalendarDayMarks
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.RoutinePalette
import com.kaushalkr.axiom.feature.academics.components.ExamCountdownRow
import com.kaushalkr.axiom.feature.routine.RoutineMonthUi
import java.time.LocalDate
import java.time.YearMonth

// ===== SECTION: MONTH_OVERVIEW =====
/**
 * The month view: a calendar with a dot per planned kind (and a danger dot for exams), then the
 * month's exams and deadlines. Tapping a day opens its timeline.
 */
@Composable
fun MonthOverview(
    month: RoutineMonthUi,
    selectedDate: LocalDate,
    today: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onOpenDay: (LocalDate) -> Unit,
    onOpenExam: (examId: Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    // Theme-dependent dots, rebuilt only when the data or theme changes.
    val marks = remember(month.marks, colors) {
        month.marks.mapValues { (_, mark) ->
            val dots = buildList {
                if (mark.examCount > 0) add(colors.danger)
                mark.kinds.forEach { add(RoutinePalette.color(it, colors)) }
            }
            if (dots.isEmpty()) null else CalendarDayMarks(dots = dots)
        }
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.lg),
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth(), hostsGlass = true) {
            AxiomMonthGrid(
                month = month.month,
                onMonthChange = onMonthChange,
                selectedDate = selectedDate,
                today = today,
                onSelect = onOpenDay,
                previousMonthLabel = stringResource(R.string.calendar_previous_month),
                nextMonthLabel = stringResource(R.string.calendar_next_month),
                marks = { date -> marks[date] },
            )
        }
        GlassCard(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            Text(
                text = stringResource(R.string.routine_month_exams_title),
                style = AxiomTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            if (month.exams.isEmpty()) {
                Text(
                    text = stringResource(R.string.routine_month_no_exams),
                    style = AxiomTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            } else {
                month.exams.forEach { exam ->
                    ExamCountdownRow(exam = exam, today = today, onClick = { onOpenExam(exam.id) })
                }
            }
        }
    }
}
// ===== END SECTION: MONTH_OVERVIEW =====
