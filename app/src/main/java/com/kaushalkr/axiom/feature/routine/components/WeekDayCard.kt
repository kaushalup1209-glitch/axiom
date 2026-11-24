package com.kaushalkr.axiom.feature.routine.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.tabularNumbers
import com.kaushalkr.axiom.core.domain.routine.RoutineOccurrence
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.home.rememberFormattedDate
import com.kaushalkr.axiom.feature.routine.RoutineWeekDayUi
import com.kaushalkr.axiom.feature.routine.color
import com.kaushalkr.axiom.feature.routine.rememberMinuteFormatter
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: WEEK_DAY_CARD =====
/**
 * One day of the week view on lightweight glass: the day, its planned time, up to
 * [MAX_ROWS] blocks and any exams. Tap the heading to open the day timeline.
 */
@Composable
fun WeekDayCard(
    day: RoutineWeekDayUi,
    isToday: Boolean,
    onOpenDay: () -> Unit,
    onOpenBlock: (blockId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val starts = day.occurrences.filter { !it.isContinuation }
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = GlassShapes.Tile,
        spec = GlassTokens.ListItem,
        contentPadding = PaddingValues(horizontal = AxiomSpacing.md, vertical = AxiomSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xxs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RowShape)
                .clickable(role = Role.Button, onClick = onOpenDay)
                .defaultMinSize(minHeight = AxiomDimens.minTouchTarget),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
        ) {
            Text(
                text = rememberFormattedDate(day.date),
                style = AxiomTheme.typography.titleSmall,
                color = if (isToday) colors.accent else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            if (isToday) AxiomPill(text = stringResource(R.string.view_today))
            Text(
                text = if (day.plannedMinutes > 0) formatStudyMinutes(day.plannedMinutes) else stringResource(R.string.routine_free_day),
                style = AxiomTheme.typography.labelLarge,
                color = colors.textSecondary,
            )
        }
        starts.take(MAX_ROWS).forEach { occurrence ->
            WeekBlockRow(occurrence = occurrence, onClick = { onOpenBlock(occurrence.block.id) })
        }
        if (starts.size > MAX_ROWS) {
            Text(
                text = pluralStringResource(R.plurals.routine_more_blocks, starts.size - MAX_ROWS, starts.size - MAX_ROWS),
                style = AxiomTheme.typography.labelLarge,
                color = colors.accent,
                modifier = Modifier
                    .clip(RowShape)
                    .clickable(role = Role.Button, onClick = onOpenDay)
                    .padding(vertical = AxiomSpacing.xxs),
            )
        }
        day.exams.forEach { exam ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                Icon(painter = painterResource(R.drawable.ic_exam), contentDescription = null, tint = colors.danger, modifier = Modifier.size(16.dp))
                Text(text = exam.title, style = AxiomTheme.typography.bodyMedium, color = colors.danger, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
// ===== END SECTION: WEEK_DAY_CARD =====

// ===== SECTION: WEEK_BLOCK_ROW =====
@Composable
private fun WeekBlockRow(occurrence: RoutineOccurrence, onClick: () -> Unit) {
    val colors = AxiomTheme.colors
    val format = rememberMinuteFormatter()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RowShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs),
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(occurrence.block.color(colors)),
        )
        Text(
            text = format(occurrence.block.startMinute),
            style = AxiomTheme.typography.labelLarge.tabularNumbers(),
            color = colors.textSecondary,
            modifier = Modifier.width(64.dp),
        )
        Text(
            text = occurrence.block.title,
            style = AxiomTheme.typography.bodyMedium,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatStudyMinutes(occurrence.block.durationMinutes),
            style = AxiomTheme.typography.bodySmall,
            color = colors.textTertiary,
        )
    }
}

private val RowShape = RoundedRectangle(12.dp)
private const val MAX_ROWS = 5
// ===== END SECTION: WEEK_BLOCK_ROW =====
