package com.kaushalkr.axiom.feature.routine.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.academics.components.ExamCountdownRow
import com.kaushalkr.axiom.feature.routine.RoutineDayUi
import java.time.LocalDate

// ===== SECTION: DAY_AGENDA_HEADER =====
/**
 * Above the day timeline: the day's exams and deadlines, and a hint on how to add blocks when
 * the day is empty. Draws nothing when there is neither.
 */
@Composable
fun DayAgendaHeader(
    day: RoutineDayUi,
    today: LocalDate,
    hasAnyBlocks: Boolean,
    onOpenExam: (examId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showHint = day.occurrences.isEmpty()
    if (day.exams.isEmpty() && !showHint) return
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = GlassShapes.Tile,
        spec = GlassTokens.ListItem,
        contentPadding = PaddingValues(horizontal = AxiomSpacing.md, vertical = AxiomSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xxs),
    ) {
        day.exams.forEach { exam ->
            ExamCountdownRow(exam = exam, today = today, onClick = { onOpenExam(exam.id) })
        }
        if (showHint) {
            Text(
                text = stringResource(if (hasAnyBlocks) R.string.routine_day_empty_hint else R.string.routine_first_block_hint),
                style = AxiomTheme.typography.bodyMedium,
                color = AxiomTheme.colors.textSecondary,
            )
        }
    }
}
// ===== END SECTION: DAY_AGENDA_HEADER =====
