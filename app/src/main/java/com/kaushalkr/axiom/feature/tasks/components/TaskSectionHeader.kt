package com.kaushalkr.axiom.feature.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: TASK_SECTION_HEADER =====
/** Group heading in the task list ("Tomorrow", "Friday, 28 November", "No date"). */
@Composable
fun TaskSectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = AxiomSpacing.xxs, end = AxiomSpacing.xxs, top = AxiomSpacing.xs)
            .semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = AxiomTheme.typography.titleSmall, color = colors.textSecondary)
        Text(text = count.toString(), style = AxiomTheme.typography.labelMedium, color = colors.textTertiary)
    }
}
// ===== END SECTION: TASK_SECTION_HEADER =====
