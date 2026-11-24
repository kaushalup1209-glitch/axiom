package com.kaushalkr.axiom.feature.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassBottomSheet
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.tasks.TaskSort
import com.kaushalkr.axiom.feature.tasks.labelRes
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: TASK_SORT_SHEET =====
/** Sort options in a Liquid Glass sheet. Choosing one applies it and closes the sheet. */
@Composable
fun TaskSortSheet(
    visible: Boolean,
    sort: TaskSort,
    onSelect: (TaskSort) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AxiomTheme.colors
    GlassBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.tasks_sort),
        closeLabel = stringResource(R.string.sheet_close),
    ) {
        Column(modifier = Modifier.selectableGroup()) {
            TaskSort.entries.forEach { option ->
                val selected = option == sort
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedRectangle(14.dp))
                        .selectable(selected = selected, role = Role.RadioButton) {
                            onSelect(option)
                            onDismiss()
                        }
                        .defaultMinSize(minHeight = AxiomDimens.minTouchTarget)
                        .padding(horizontal = AxiomSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(option.labelRes()),
                        style = AxiomTheme.typography.bodyLarge,
                        color = if (selected) colors.accent else colors.textPrimary,
                    )
                    if (selected) {
                        Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        Text(text = stringResource(R.string.sort_reorder_hint), style = AxiomTheme.typography.bodySmall, color = colors.textTertiary)
    }
}
// ===== END SECTION: TASK_SORT_SHEET =====
