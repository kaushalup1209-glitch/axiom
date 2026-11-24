package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomChip
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.model.Weekdays
import com.kaushalkr.axiom.feature.routine.editor.RepeatMode
import com.kaushalkr.axiom.feature.routine.rememberWeekdayOrder
import com.kaushalkr.axiom.feature.routine.weekdayShortName
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorFieldLabel
import com.kaushalkr.axiom.feature.tasks.editor.components.EditorSectionCard
import com.kaushalkr.axiom.feature.tasks.rememberShortDateFormatter
import java.time.DayOfWeek
import java.time.LocalDate

// ===== SECTION: ROUTINE_DAYS_CARD =====
/** Repeat on chosen weekdays (with quick sets), or happen once on a picked date. */
@Composable
fun RoutineDaysCard(
    repeatMode: RepeatMode,
    daysMask: Int,
    oneOffDate: LocalDate,
    onSetDays: (Int) -> Unit,
    onToggleDay: (DayOfWeek) -> Unit,
    onUseOneDay: () -> Unit,
    onPickDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekly = repeatMode == RepeatMode.WEEKLY
    val dateFormatter = rememberShortDateFormatter()
    EditorSectionCard(iconRes = R.drawable.ic_repeat, title = stringResource(R.string.editor_repeat), modifier = modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            AxiomChip(
                label = stringResource(R.string.routine_repeat_every_day),
                selected = weekly && daysMask == Weekdays.EVERY_DAY,
                onClick = { onSetDays(Weekdays.EVERY_DAY) },
                role = Role.RadioButton,
            )
            AxiomChip(
                label = stringResource(R.string.routine_repeat_weekdays),
                selected = weekly && daysMask == Weekdays.MONDAY_TO_FRIDAY,
                onClick = { onSetDays(Weekdays.MONDAY_TO_FRIDAY) },
                role = Role.RadioButton,
            )
            AxiomChip(
                label = stringResource(R.string.routine_repeat_weekends),
                selected = weekly && daysMask == Weekdays.WEEKEND,
                onClick = { onSetDays(Weekdays.WEEKEND) },
                role = Role.RadioButton,
            )
            AxiomChip(
                label = if (weekly) stringResource(R.string.routine_repeat_one_day) else dateFormatter.format(oneOffDate),
                selected = !weekly,
                onClick = { if (weekly) onUseOneDay() else onPickDate() },
                iconRes = R.drawable.ic_calendar,
                role = Role.RadioButton,
            )
        }
        if (weekly) {
            EditorFieldLabel(stringResource(R.string.routine_editor_on_days))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                rememberWeekdayOrder().forEach { day ->
                    AxiomChip(
                        label = weekdayShortName(day),
                        selected = Weekdays.contains(daysMask, day),
                        onClick = { onToggleDay(day) },
                        role = Role.Checkbox,
                    )
                }
            }
        } else {
            EditorFieldLabel(stringResource(R.string.routine_editor_one_day_hint))
        }
    }
}
// ===== END SECTION: ROUTINE_DAYS_CARD =====
