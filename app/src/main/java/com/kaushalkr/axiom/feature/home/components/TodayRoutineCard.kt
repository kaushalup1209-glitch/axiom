package com.kaushalkr.axiom.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.data.sample.PreviewRoutineBlock
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.tabularNumbers
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.home.formatTime
import com.kaushalkr.axiom.feature.home.rememberTimeFormatter
import java.time.LocalTime

// ===== SECTION: HOME_TODAY_ROUTINE_CARD =====
/** Today's routine blocks with the next one highlighted. Shows PLACEHOLDER data until Phase 04. */
@Composable
fun TodayRoutineCard(
    routine: List<PreviewRoutineBlock>,
    currentTime: LocalTime,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val nextIndex = routine.indexOfFirst { block -> block.start.plusMinutes(block.durationMinutes.toLong()) > currentTime }
    GlassCard(modifier = modifier) {
        HomeCardHeader(
            iconRes = R.drawable.ic_routine,
            title = stringResource(R.string.home_routine_title),
            trailing = { AxiomPill(text = stringResource(R.string.home_preview_badge)) },
        )
        Spacer(Modifier.height(AxiomSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            routine.forEachIndexed { index, block ->
                RoutineRow(block = block, isNext = index == nextIndex, isPast = nextIndex == -1 || index < nextIndex)
            }
        }
        Spacer(Modifier.height(AxiomSpacing.md))
        Text(
            text = stringResource(R.string.home_routine_preview_note),
            style = AxiomTheme.typography.bodySmall,
            color = colors.textTertiary,
        )
    }
}
// ===== END SECTION: HOME_TODAY_ROUTINE_CARD =====

// ===== SECTION: HOME_ROUTINE_ROW =====
@Composable
private fun RoutineRow(block: PreviewRoutineBlock, isNext: Boolean, isPast: Boolean) {
    val colors = AxiomTheme.colors
    val timeFormatter = rememberTimeFormatter()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isPast) 0.45f else 1f)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Text(
            text = timeFormatter.formatTime(block.start),
            style = AxiomTheme.typography.labelLarge.tabularNumbers(),
            color = if (isNext) colors.accent else colors.textSecondary,
            modifier = Modifier.width(68.dp),
        )
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isNext) colors.accent else colors.controlTrack),
        )
        Column(modifier = Modifier.weight(1f).padding(start = AxiomSpacing.xxs)) {
            Text(text = block.title, style = AxiomTheme.typography.titleSmall, color = colors.textPrimary)
            Text(
                text = stringResource(R.string.home_routine_detail, block.subject, formatStudyMinutes(block.durationMinutes)),
                style = AxiomTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        if (isNext) AxiomPill(text = stringResource(R.string.home_routine_up_next))
    }
}
// ===== END SECTION: HOME_ROUTINE_ROW =====
