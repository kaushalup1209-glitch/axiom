package com.kaushalkr.axiom.feature.routine.editor.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassBottomSheet
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.domain.routine.ConflictResolutions
import com.kaushalkr.axiom.core.domain.routine.RoutineConflict
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.routine.rememberMinuteFormatter

// ===== SECTION: ROUTINE_CONFLICT_SHEET =====
/**
 * Shown when saving a block that overlaps others: move it to the next free time, shorten it to
 * fit, keep both anyway, or go back to editing. Fixes that are not possible are not offered.
 */
@Composable
fun RoutineConflictSheet(
    visible: Boolean,
    conflicts: List<RoutineConflict>,
    resolutions: ConflictResolutions?,
    onMove: () -> Unit,
    onShorten: () -> Unit,
    onSaveAnyway: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val format = rememberMinuteFormatter()
    GlassBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.routine_conflict_sheet_title),
        closeLabel = stringResource(R.string.sheet_close),
    ) {
        Text(
            text = stringResource(R.string.routine_conflict_sheet_body, conflictList(conflicts)),
            style = AxiomTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        resolutions?.nextFreeStartMinute?.let { start ->
            GlassButton(
                onClick = {
                    onDismiss()
                    onMove()
                },
                modifier = Modifier.fillMaxWidth(),
                tint = colors.accent,
            ) {
                Text(stringResource(R.string.routine_conflict_move, format(start)))
            }
        }
        resolutions?.shortenedDurationMinutes?.let { minutes ->
            GlassButton(
                onClick = {
                    onDismiss()
                    onShorten()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.routine_conflict_shorten, formatStudyMinutes(minutes)))
            }
        }
        GlassButton(
            onClick = {
                onDismiss()
                onSaveAnyway()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.routine_conflict_keep_both))
        }
        GlassButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.dialog_discard_keep))
        }
    }
}
// ===== END SECTION: ROUTINE_CONFLICT_SHEET =====
