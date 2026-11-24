package com.kaushalkr.axiom.feature.routine.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.feature.routine.RoutineDayUi
import com.kaushalkr.axiom.feature.routine.RoutineViewModel
import com.kaushalkr.axiom.feature.routine.rememberMinuteFormatter

// ===== SECTION: DAY_TIMELINE =====
/**
 * The day view: a 24-hour timeline with hour lines, blocks placed by time (overlapping blocks
 * share the width in lanes), and a current-time line on today. Tap an empty time to add a
 * block there. The hour grid is one draw call; blocks are positioned at composition, so
 * scrolling only moves layers.
 */
@Composable
fun DayTimeline(
    day: RoutineDayUi,
    nowMinute: Int?,
    onOpenBlock: (blockId: Long) -> Unit,
    onMoveBlock: (blockId: Long, deltaMinutes: Int) -> Unit,
    onDeleteBlock: (blockId: Long, title: String) -> Unit,
    onCreateAt: (minute: Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
) {
    val colors = AxiomTheme.colors
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val minuteHeight = HourHeight / 60f
    val minutePx = with(density) { minuteHeight.toPx() }
    val formatMinute = rememberMinuteFormatter()
    val latestOnCreateAt by rememberUpdatedState(onCreateAt)

    // ===== SECTION: DAY_TIMELINE_INITIAL_SCROLL =====
    // Open each day at the current time (today) or its first block, an hour above it.
    LaunchedEffect(day.date) {
        val focus = nowMinute ?: day.occurrences.minOfOrNull { it.occurrence.startMinute } ?: DEFAULT_FOCUS_MINUTE
        scrollState.scrollTo(((focus - SCROLL_LEAD_MINUTES).coerceAtLeast(0) * minutePx).toInt())
    }
    // ===== END SECTION: DAY_TIMELINE_INITIAL_SCROLL =====

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        header()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(HourHeight * HOURS + GridBottomPadding),
        ) {
            val labelWidthPx = with(density) { LabelWidth.toPx() }
            val gridColor = colors.divider
            val blockAreaWidth = maxWidth - LabelWidth - BlockGap

            // ===== SECTION: DAY_TIMELINE_GRID =====
            Box(
                Modifier
                    .matchParentSize()
                    .drawBehind {
                        val stroke = 1.dp.toPx()
                        for (hour in 0..HOURS) {
                            val y = hour * 60 * minutePx
                            drawLine(gridColor, Offset(labelWidthPx, y), Offset(size.width, y), strokeWidth = stroke)
                        }
                    }
                    .pointerInput(day.date, minutePx) {
                        detectTapGestures { offset ->
                            if (offset.x > labelWidthPx) {
                                val minute = (offset.y / minutePx).toInt()
                                val snapped = (minute / CREATE_SNAP_MINUTES) * CREATE_SNAP_MINUTES
                                latestOnCreateAt(snapped.coerceIn(0, MINUTES_PER_DAY - RoutineLimits.SNAP_MINUTES))
                            }
                        }
                    },
            )
            for (hour in 0 until HOURS) {
                Text(
                    text = formatMinute(hour * 60),
                    style = AxiomTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .offset(y = HourHeight * hour - if (hour == 0) 0.dp else LabelNudge)
                        .width(LabelWidth - AxiomSpacing.xs)
                        .clearAndSetSemantics { },
                )
            }
            // ===== END SECTION: DAY_TIMELINE_GRID =====

            // ===== SECTION: DAY_TIMELINE_BLOCKS =====
            day.occurrences.forEach { laned ->
                key(laned.occurrence.key) {
                    val occurrence = laned.occurrence
                    val laneWidth = (blockAreaWidth - BlockGap * (laned.laneCount - 1)) / laned.laneCount
                    val height = max(minuteHeight * occurrence.durationMinutes, minuteHeight * RoutineViewModel.MIN_VISUAL_MINUTES) - 2.dp
                    RoutineTimelineBlock(
                        laned = laned,
                        x = LabelWidth + BlockGap + (laneWidth + BlockGap) * laned.lane,
                        y = minuteHeight * occurrence.startMinute + 1.dp,
                        width = laneWidth,
                        height = height,
                        minuteHeight = minuteHeight,
                        isOngoing = nowMinute != null && nowMinute >= occurrence.startMinute && nowMinute < occurrence.endMinute,
                        onOpen = { onOpenBlock(occurrence.block.id) },
                        onMove = { delta -> onMoveBlock(occurrence.block.id, delta) },
                        onDelete = { onDeleteBlock(occurrence.block.id, occurrence.block.title) },
                    )
                }
            }
            // ===== END SECTION: DAY_TIMELINE_BLOCKS =====

            if (nowMinute != null) {
                NowIndicator(
                    label = stringResource(R.string.routine_now),
                    modifier = Modifier.offset(x = LabelWidth - NowDotSize / 2, y = minuteHeight * nowMinute - NowRowHeight / 2),
                    width = maxWidth - LabelWidth + NowDotSize / 2,
                )
            }
        }
    }
}
// ===== END SECTION: DAY_TIMELINE =====

// ===== SECTION: DAY_TIMELINE_NOW =====
@Composable
private fun NowIndicator(label: String, width: Dp, modifier: Modifier = Modifier) {
    val accent = AxiomTheme.colors.accent
    Row(modifier = modifier.width(width).height(NowRowHeight), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(NowDotSize)
                .clip(CircleShape)
                .background(accent)
                .clearAndSetSemantics { },
        )
        Box(
            Modifier
                .weight(1f)
                .height(2.dp)
                .background(accent),
        )
        Text(
            text = label,
            style = AxiomTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
// ===== END SECTION: DAY_TIMELINE_NOW =====

private const val HOURS = 24
private const val DEFAULT_FOCUS_MINUTE = 7 * 60
private const val SCROLL_LEAD_MINUTES = 60
private const val CREATE_SNAP_MINUTES = 30
private val HourHeight = 64.dp
private val LabelWidth = 52.dp
private val LabelNudge = 7.dp
private val BlockGap = 4.dp
private val GridBottomPadding = 8.dp
private val NowDotSize = 10.dp
private val NowRowHeight = 16.dp
