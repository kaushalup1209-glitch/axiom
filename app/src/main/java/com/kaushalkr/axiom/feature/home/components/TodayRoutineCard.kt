package com.kaushalkr.axiom.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomLinearProgress
import com.kaushalkr.axiom.core.designsystem.components.AxiomPill
import com.kaushalkr.axiom.core.designsystem.components.GlassButton
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.designsystem.theme.tabularNumbers
import com.kaushalkr.axiom.core.domain.routine.RoutineNow
import com.kaushalkr.axiom.core.domain.routine.RoutineOccurrence
import com.kaushalkr.axiom.core.model.RoutineKind
import com.kaushalkr.axiom.feature.home.formatStudyMinutes
import com.kaushalkr.axiom.feature.routine.color
import com.kaushalkr.axiom.feature.routine.labelRes
import com.kaushalkr.axiom.feature.routine.rememberMinuteFormatter
import com.kaushalkr.axiom.feature.routine.timeRangeText
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: HOME_TODAY_ROUTINE_CARD =====
/**
 * Real timetable data: the block running now with its progress, the next few blocks today,
 * and tomorrow's first block once today is over. Rows open the block editor.
 */
@Composable
fun TodayRoutineCard(
    routine: RoutineNow,
    currentMinute: Int,
    onOpenBlock: (blockId: Long) -> Unit,
    onOpenTimetable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val isTodayOver = routine.ongoing.isEmpty() && routine.upcoming.isEmpty()
    GlassCard(modifier = modifier, hostsGlass = true) {
        HomeCardHeader(
            iconRes = R.drawable.ic_routine,
            title = stringResource(R.string.home_routine_title),
            subtitle = if (routine.todayCount > 0) {
                pluralStringResource(R.plurals.home_routine_today_count, routine.todayCount, routine.todayCount)
            } else {
                null
            },
        )
        Spacer(Modifier.height(AxiomSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm)) {
            routine.ongoing.take(MAX_ONGOING_ROWS).forEach { occurrence ->
                key(occurrence.key) {
                    OngoingBlockRow(occurrence = occurrence, currentMinute = currentMinute, onClick = { onOpenBlock(occurrence.block.id) })
                }
            }
            routine.upcoming.forEachIndexed { index, occurrence ->
                key(occurrence.key) {
                    UpcomingBlockRow(
                        occurrence = occurrence,
                        isNext = index == 0 && routine.ongoing.isEmpty(),
                        onClick = { onOpenBlock(occurrence.block.id) },
                    )
                }
            }
            if (isTodayOver) {
                Text(
                    text = stringResource(if (routine.todayCount > 0) R.string.home_routine_done_today else R.string.home_routine_empty),
                    style = AxiomTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = AxiomSpacing.xs),
                )
                val tomorrow = routine.tomorrowFirst
                if (tomorrow != null) {
                    Text(
                        text = stringResource(R.string.section_tomorrow),
                        style = AxiomTheme.typography.labelLarge,
                        color = colors.textSecondary,
                    )
                    key(tomorrow.key) {
                        UpcomingBlockRow(occurrence = tomorrow, isNext = false, onClick = { onOpenBlock(tomorrow.block.id) })
                    }
                }
            }
        }
        Spacer(Modifier.height(AxiomSpacing.md))
        GlassButton(onClick = onOpenTimetable, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_routine_open))
            Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

/** More than two blocks at once is rare; the timetable shows the rest side by side. */
private const val MAX_ONGOING_ROWS = 2
// ===== END SECTION: HOME_TODAY_ROUTINE_CARD =====

// ===== SECTION: HOME_ROUTINE_ONGOING_ROW =====
/** The block running now: colour bar, title, time range, progress and time left. */
@Composable
private fun OngoingBlockRow(
    occurrence: RoutineOccurrence,
    currentMinute: Int,
    onClick: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val block = occurrence.block
    val blockColor = block.color(colors)
    val elapsed = occurrence.elapsedMinutes(currentMinute)
    val remaining = occurrence.remainingMinutes(currentMinute)
    val progress = if (block.durationMinutes == 0) 0f else elapsed.toFloat() / block.durationMinutes
    val linked = block.linkedTasks

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RowShape)
            .background(blockColor.copy(alpha = ONGOING_TINT_ALPHA))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(AxiomSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(blockColor),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AxiomSpacing.xxs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
                Text(
                    text = block.title,
                    style = AxiomTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                AxiomPill(text = stringResource(R.string.routine_now))
            }
            Text(
                text = timeRangeText(block.startMinute, block.endMinute),
                style = AxiomTheme.typography.bodySmall.tabularNumbers(),
                color = colors.textSecondary,
            )
            AxiomLinearProgress(progress = progress, height = 6.dp, animate = false, modifier = Modifier.padding(vertical = AxiomSpacing.xxs))
            val left = stringResource(R.string.home_routine_left, formatStudyMinutes(remaining))
            val tasks = if (linked.isEmpty()) {
                null
            } else {
                pluralStringResource(R.plurals.routine_linked_tasks, linked.size, linked.count { it.isCompleted }, linked.size)
            }
            Text(
                text = listOfNotNull(left, tasks).joinToString(" · "),
                style = AxiomTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
        }
    }
}

private const val ONGOING_TINT_ALPHA = 0.12f
// ===== END SECTION: HOME_ROUTINE_ONGOING_ROW =====

// ===== SECTION: HOME_ROUTINE_UPCOMING_ROW =====
/** A later block: start time, colour dot, title and subject (or kind) with its length. */
@Composable
private fun UpcomingBlockRow(
    occurrence: RoutineOccurrence,
    isNext: Boolean,
    onClick: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val block = occurrence.block
    val formatMinute = rememberMinuteFormatter()
    val subjectName = block.subject?.name?.takeIf { block.kind == RoutineKind.STUDY }
    val detail = stringResource(
        R.string.home_routine_detail,
        subjectName ?: stringResource(block.kind.labelRes()),
        formatStudyMinutes(block.durationMinutes),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RowShape)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = AxiomDimens.minTouchTarget)
            .padding(vertical = AxiomSpacing.xxs, horizontal = AxiomSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Text(
            text = formatMinute(block.startMinute),
            style = AxiomTheme.typography.labelLarge.tabularNumbers(),
            color = if (isNext) colors.accent else colors.textSecondary,
            maxLines = 1,
            modifier = Modifier.width(68.dp),
        )
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(block.color(colors)),
        )
        Column(modifier = Modifier.weight(1f).padding(start = AxiomSpacing.xxs)) {
            Text(
                text = block.title,
                style = AxiomTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = AxiomTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isNext) AxiomPill(text = stringResource(R.string.home_routine_up_next))
    }
}

private val RowShape = RoundedRectangle(12.dp)
// ===== END SECTION: HOME_ROUTINE_UPCOMING_ROW =====
