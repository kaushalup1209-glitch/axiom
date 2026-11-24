package com.kaushalkr.axiom.feature.routine.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.domain.routine.LanedOccurrence
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.feature.routine.color
import com.kaushalkr.axiom.feature.routine.iconRes
import com.kaushalkr.axiom.feature.routine.labelRes
import com.kaushalkr.axiom.feature.routine.rememberMinuteFormatter
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ===== SECTION: ROUTINE_TIMELINE_BLOCK =====
/**
 * One block on the day timeline, on lightweight glass ([GlassTokens.ListItem]). Tap to edit;
 * touch and hold, then drag up or down to move it (5-minute snaps, with the new time shown
 * while dragging). Moving changes every day the block repeats on and can be undone.
 *
 * The drag offset is applied in a graphics layer, so dragging never relayouts the timeline.
 */
@Composable
fun RoutineTimelineBlock(
    laned: LanedOccurrence,
    x: Dp,
    y: Dp,
    width: Dp,
    height: Dp,
    minuteHeight: Dp,
    isOngoing: Boolean,
    onOpen: () -> Unit,
    onMove: (deltaMinutes: Int) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val occurrence = laned.occurrence
    val block = occurrence.block
    val accent = block.color(colors)
    val haptics = LocalHapticFeedback.current
    val minutePx = with(LocalDensity.current) { minuteHeight.toPx() }
    val formatMinute = rememberMinuteFormatter()
    val latestOnMove by rememberUpdatedState(onMove)
    val latestOnDelete by rememberUpdatedState(onDelete)

    // ===== SECTION: ROUTINE_TIMELINE_BLOCK_DRAG =====
    var dragging by remember { mutableStateOf(false) }
    var dragPx by remember { mutableFloatStateOf(0f) }
    // Holds the dropped position until the saved time arrives, so the block does not jump back.
    var pendingMinutes by remember(occurrence.startMinute) { mutableIntStateOf(0) }
    LaunchedEffect(pendingMinutes) {
        if (pendingMinutes != 0) {
            delay(PENDING_RESET_MILLIS)
            pendingMinutes = 0
        }
    }
    val minDelta = -block.startMinute
    val maxDelta = MINUTES_PER_DAY - 1 - block.startMinute
    val snappedDelta = snap((dragPx / minutePx).roundToInt()).coerceIn(minDelta, maxDelta)
    // ===== END SECTION: ROUTINE_TIMELINE_BLOCK_DRAG =====

    val title = block.title
    val range = stringResource(
        R.string.routine_time_range,
        formatMinute(block.startMinute + if (dragging) snappedDelta else 0),
        formatMinute(block.endMinute + if (dragging) snappedDelta else 0),
    )
    val moveEarlier = stringResource(R.string.routine_action_move_earlier)
    val moveLater = stringResource(R.string.routine_action_move_later)
    val deleteLabel = stringResource(R.string.task_action_delete)
    val description = stringResource(R.string.routine_block_description, title, range, stringResource(block.kind.labelRes()))
    val actions = remember(moveEarlier, moveLater, deleteLabel) {
        listOf(
            CustomAccessibilityAction(moveEarlier) { latestOnMove(-ACCESSIBILITY_STEP_MINUTES); true },
            CustomAccessibilityAction(moveLater) { latestOnMove(ACCESSIBILITY_STEP_MINUTES); true },
            CustomAccessibilityAction(deleteLabel) { latestOnDelete(); true },
        )
    }

    GlassCard(
        modifier = Modifier
            .offset(x = x, y = y)
            .width(width)
            .height(height)
            .zIndex(if (dragging) 1f else 0f)
            .graphicsLayer {
                translationY = if (dragging) dragPx.coerceIn(minDelta * minutePx, maxDelta * minutePx) else pendingMinutes * minutePx
                val scale = if (dragging) DRAG_SCALE else 1f
                scaleX = scale
                scaleY = scale
            }
            .then(if (isOngoing) Modifier.border(2.dp, accent, BlockShape) else Modifier)
            .pointerInput(block.id, block.startMinute, minutePx) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dragPx = 0f
                        dragging = true
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragPx += amount.y
                    },
                    onDragEnd = {
                        val delta = snap((dragPx / minutePx).roundToInt()).coerceIn(-block.startMinute, MINUTES_PER_DAY - 1 - block.startMinute)
                        dragging = false
                        dragPx = 0f
                        if (delta != 0) {
                            pendingMinutes = delta
                            latestOnMove(delta)
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        dragPx = 0f
                    },
                )
            }
            .semantics(mergeDescendants = true) {
                contentDescription = description
                customActions = actions
            },
        shape = BlockShape,
        spec = GlassTokens.ListItem,
        contentPadding = PaddingValues(0.dp),
        onClick = onOpen,
    ) {
        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            BlockContent(laned = laned, range = range, title = title, heightDp = height, iconTint = accent)
        }
    }
}

@Composable
private fun BlockContent(laned: LanedOccurrence, range: String, title: String, heightDp: Dp, iconTint: Color) {
    val colors = AxiomTheme.colors
    val block = laned.occurrence.block
    val roomy = heightDp >= ROOMY_HEIGHT
    val tall = heightDp >= TALL_HEIGHT
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AxiomSpacing.xs, vertical = if (roomy) 6.dp else 2.dp),
        verticalAlignment = if (roomy) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = if (laned.occurrence.isContinuation) stringResource(R.string.routine_block_continued, title) else title,
                style = AxiomTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = if (tall) 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (roomy) {
                Text(text = range, style = AxiomTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1)
            }
            if (tall) {
                val detail = listOfNotNull(
                    block.subject?.name,
                    block.linkedTasks.takeIf { it.isNotEmpty() }?.let { tasks ->
                        pluralStringResource(R.plurals.routine_linked_tasks, tasks.size, tasks.count { it.isCompleted }, tasks.size)
                    },
                ).joinToString(" · ")
                if (detail.isNotEmpty()) {
                    Text(text = detail, style = AxiomTheme.typography.bodySmall, color = colors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (laned.laneCount == 1 || roomy) {
            Icon(
                painter = painterResource(block.kind.iconRes()),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun snap(minutes: Int): Int =
    (minutes.toFloat() / RoutineLimits.SNAP_MINUTES).roundToInt() * RoutineLimits.SNAP_MINUTES

/** Shared shape instance, so glass modifiers can be remembered. */
private val BlockShape = RoundedRectangle(14.dp)
private val ROOMY_HEIGHT = 44.dp
private val TALL_HEIGHT = 76.dp
private const val DRAG_SCALE = 1.03f
private const val PENDING_RESET_MILLIS = 1_500L
private const val ACCESSIBILITY_STEP_MINUTES = 15
// ===== END SECTION: ROUTINE_TIMELINE_BLOCK =====
