package com.kaushalkr.axiom.feature.tasks.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomChip
import com.kaushalkr.axiom.core.designsystem.components.GlassSegmentedControl
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.model.TaskLimits
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.tasks.labelRes

// ===== SECTION: TASK_PRIORITY_EFFORT_CARD =====
/** Priority on a glass segmented control, and the estimated duration as quick choices. */
@Composable
fun TaskPriorityEffortCard(
    priority: TaskPriority,
    estimatedMinutes: Int?,
    onSelectPriority: (TaskPriority) -> Unit,
    onSelectEstimate: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A value set elsewhere (e.g. by the assistant) that is not a preset still shows as selected.
    val presets = TaskLimits.ESTIMATE_PRESETS_MINUTES.let { list ->
        if (estimatedMinutes != null && estimatedMinutes !in list) (list + estimatedMinutes).sorted() else list
    }
    EditorSectionCard(
        iconRes = R.drawable.ic_priority,
        title = stringResource(R.string.editor_priority_effort),
        modifier = modifier,
        hostsGlass = true,
    ) {
        EditorFieldLabel(stringResource(R.string.filter_priority))
        GlassSegmentedControl(
            options = TaskPriority.entries.map { stringResource(it.labelRes()) },
            selectedIndex = priority.ordinal,
            onSelect = { index -> onSelectPriority(TaskPriority.entries[index]) },
            modifier = Modifier.fillMaxWidth(),
        )
        EditorFieldLabel(stringResource(R.string.editor_estimate))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            AxiomChip(
                label = stringResource(R.string.editor_estimate_none),
                selected = estimatedMinutes == null,
                onClick = { onSelectEstimate(null) },
                role = Role.RadioButton,
            )
            presets.forEach { minutes ->
                AxiomChip(
                    label = formatStudyMinutes(minutes),
                    selected = estimatedMinutes == minutes,
                    onClick = { onSelectEstimate(minutes) },
                    role = Role.RadioButton,
                )
            }
        }
    }
}
// ===== END SECTION: TASK_PRIORITY_EFFORT_CARD =====
