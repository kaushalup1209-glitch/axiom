package com.kaushalkr.axiom.feature.tasks.editor.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomMonthCalendar
import com.kaushalkr.axiom.core.designsystem.components.GlassBottomSheet
import java.time.LocalDate

// ===== SECTION: TASK_DATE_SHEET =====
/** Calendar in a Liquid Glass sheet. Tapping a day sets it and closes the sheet. */
@Composable
fun TaskDateSheet(
    visible: Boolean,
    selectedDate: LocalDate?,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    GlassBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.editor_date_sheet_title),
        closeLabel = stringResource(R.string.sheet_close),
    ) {
        AxiomMonthCalendar(
            selectedDate = selectedDate,
            today = today,
            onSelect = { date ->
                onSelect(date)
                onDismiss()
            },
            previousMonthLabel = stringResource(R.string.calendar_previous_month),
            nextMonthLabel = stringResource(R.string.calendar_next_month),
        )
    }
}
// ===== END SECTION: TASK_DATE_SHEET =====
