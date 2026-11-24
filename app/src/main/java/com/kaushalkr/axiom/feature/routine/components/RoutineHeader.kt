package com.kaushalkr.axiom.feature.routine.components

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassIconButton
import com.kaushalkr.axiom.core.designsystem.components.GlassSegmentedControl
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.home.currentUiLocale
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.home.rememberFormattedDate
import com.kaushalkr.axiom.feature.routine.RoutineUiState
import com.kaushalkr.axiom.feature.routine.RoutineView
import java.time.format.DateTimeFormatter

// ===== SECTION: ROUTINE_HEADER =====
/**
 * Fixed top of the timetable: the period shown with a short summary and a New block button,
 * then the Day / Week / Month switcher with previous and next buttons.
 */
@Composable
fun RoutineHeader(
    state: RoutineUiState?,
    onSelectView: (RoutineView) -> Unit,
    onStep: (direction: Int) -> Unit,
    onToday: () -> Unit,
    onNewBlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val view = state?.view ?: RoutineView.DAY
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (state == null) "" else periodTitle(state),
                    style = AxiomTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() },
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                    if (state != null) {
                        Text(
                            text = periodSummary(state),
                            style = AxiomTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (!state.isOnCurrentPeriod) {
                            Text(
                                text = stringResource(R.string.routine_back_to_today),
                                style = AxiomTheme.typography.labelLarge,
                                color = colors.accent,
                                modifier = Modifier
                                    .clip(GlassShapes.Capsule)
                                    .clickable(role = Role.Button, onClick = onToday)
                                    .padding(horizontal = AxiomSpacing.xs, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
            GlassButton(
                onClick = onNewBlock,
                tint = colors.accent,
                contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.routine_new_block))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
        ) {
            GlassSegmentedControl(
                options = RoutineView.entries.map { stringResource(it.labelRes()) },
                selectedIndex = view.ordinal,
                onSelect = { index -> onSelectView(RoutineView.entries[index]) },
                modifier = Modifier.weight(1f),
            )
            GlassIconButton(
                onClick = { onStep(-1) },
                iconRes = R.drawable.ic_chevron_left,
                contentDescription = stringResource(R.string.routine_previous_period),
                floating = false,
            )
            GlassIconButton(
                onClick = { onStep(1) },
                iconRes = R.drawable.ic_chevron_right,
                contentDescription = stringResource(R.string.routine_next_period),
                floating = false,
            )
        }
    }
}
// ===== END SECTION: ROUTINE_HEADER =====

// ===== SECTION: ROUTINE_HEADER_TEXT =====
private fun RoutineView.labelRes(): Int = when (this) {
    RoutineView.DAY -> R.string.routine_view_day
    RoutineView.WEEK -> R.string.routine_view_week
    RoutineView.MONTH -> R.string.routine_view_month
}

@Composable
private fun periodTitle(state: RoutineUiState): String {
    val locale = currentUiLocale()
    val shortFormatter = remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "dMMM"), locale) }
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "MMMMyyyy"), locale) }
    return when (state.view) {
        RoutineView.DAY -> rememberFormattedDate(state.selectedDate)
        RoutineView.WEEK -> {
            val first = state.week.firstOrNull()?.date ?: state.selectedDate
            val last = state.week.lastOrNull()?.date ?: state.selectedDate
            stringResource(R.string.routine_time_range, shortFormatter.format(first), shortFormatter.format(last))
        }
        RoutineView.MONTH -> monthFormatter.format(state.month.month)
    }
}

@Composable
private fun periodSummary(state: RoutineUiState): String = when (state.view) {
    RoutineView.DAY -> blocksSummary(state.day.occurrences.count { !it.occurrence.isContinuation }, state.day.plannedMinutes)
    RoutineView.WEEK -> blocksSummary(
        state.week.sumOf { day -> day.occurrences.count { !it.isContinuation } },
        state.week.sumOf { it.plannedMinutes },
    )
    RoutineView.MONTH -> {
        val count = state.month.exams.size
        if (count == 0) stringResource(R.string.routine_month_no_exams) else pluralStringResource(R.plurals.routine_month_exams, count, count)
    }
}

@Composable
private fun blocksSummary(blocks: Int, minutes: Int): String =
    if (blocks == 0) {
        stringResource(R.string.routine_nothing_planned)
    } else {
        stringResource(
            R.string.tasks_summary_joined,
            pluralStringResource(R.plurals.routine_summary_blocks, blocks, blocks),
            stringResource(R.string.routine_summary_planned, formatStudyMinutes(minutes)),
        )
    }
// ===== END SECTION: ROUTINE_HEADER_TEXT =====
