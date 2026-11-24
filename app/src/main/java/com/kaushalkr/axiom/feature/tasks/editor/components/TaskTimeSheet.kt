package com.kaushalkr.axiom.feature.tasks.editor.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassBottomSheet
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import java.time.LocalTime

// ===== SECTION: TASK_TIME_SHEET =====
/** Material clock dial on a Liquid Glass sheet; follows the device's 12/24-hour setting. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTimeSheet(
    visible: Boolean,
    initialTime: LocalTime?,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    GlassBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.editor_time_sheet_title),
        closeLabel = stringResource(R.string.sheet_close),
    ) {
        val start = initialTime ?: DEFAULT_TIME
        val pickerState = rememberTimePickerState(initialHour = start.hour, initialMinute = start.minute, is24Hour = is24Hour)
        TimePicker(
            state = pickerState,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = TimePickerDefaults.colors(
                containerColor = Color.Transparent,
                clockDialColor = colors.controlTrack,
                selectorColor = colors.accent,
                periodSelectorBorderColor = colors.divider,
                periodSelectorSelectedContainerColor = colors.accentSoft,
                timeSelectorSelectedContainerColor = colors.accentSoft,
                timeSelectorContainerColor = colors.controlTrack,
            ),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            GlassButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.dialog_cancel))
            }
            GlassButton(
                onClick = {
                    onConfirm(LocalTime.of(pickerState.hour, pickerState.minute))
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
                tint = colors.accent,
            ) {
                Text(stringResource(R.string.editor_time_set))
            }
        }
    }
}

private val DEFAULT_TIME: LocalTime = LocalTime.of(18, 0)
// ===== END SECTION: TASK_TIME_SHEET =====
