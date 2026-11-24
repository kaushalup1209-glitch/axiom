package com.kaushalkr.axiom.core.ui.reorder

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ===== SECTION: REORDERABLE_LIST_STATE =====
/**
 * Long-press-and-drag reordering for a LazyColumn, with no extra dependency.
 *
 * The dragged item stays under the finger through [offsetOf] (applied in a graphics layer, so
 * dragging never relayouts it); neighbours move via `Modifier.animateItem`. [onMove] is called
 * each time the dragged item crosses a neighbour that [canMove] accepts, and [onDragEnd] once on
 * release, where callers persist the final position. Dragging near an edge auto-scrolls.
 */
@Stable
class ReorderableListState internal constructor(
    private val listState: LazyListState,
    private val scope: CoroutineScope,
    private val autoScrollEdgePx: Float,
    private val maxAutoScrollPx: Float,
    private val onMove: (fromKey: Any, toKey: Any) -> Unit,
    private val canMove: (fromKey: Any, toKey: Any) -> Boolean,
    private val onDragEnd: (key: Any) -> Unit,
) {
    var draggingKey: Any? by mutableStateOf(null)
        private set

    private var initialOffset by mutableIntStateOf(0)
    private var draggedDelta by mutableFloatStateOf(0f)
    private var autoScrollJob: Job? = null

    /** Index of the dragged item when the last move was requested; blocks repeats until relayout. */
    private var indexAtLastMove = NO_INDEX

    private val draggingItemInfo: LazyListItemInfo?
        get() = draggingKey?.let { key -> listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } }

    /** Vertical translation for [key]: non-zero only for the item being dragged. */
    fun offsetOf(key: Any): Float {
        if (key != draggingKey) return 0f
        val item = draggingItemInfo ?: return 0f
        return initialOffset + draggedDelta - item.offset
    }

    fun isDragging(key: Any): Boolean = key == draggingKey

    // ===== SECTION: REORDERABLE_LIST_GESTURES =====
    internal fun start(key: Any) {
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        draggingKey = key
        initialOffset = item.offset
        draggedDelta = 0f
        indexAtLastMove = NO_INDEX
        autoScrollJob?.cancel()
        autoScrollJob = scope.launch { autoScrollLoop() }
    }

    internal fun drag(deltaY: Float) {
        draggedDelta += deltaY
        swapIfNeeded()
    }

    internal fun end() {
        val key = draggingKey ?: return
        autoScrollJob?.cancel()
        draggingKey = null
        draggedDelta = 0f
        onDragEnd(key)
    }

    private fun swapIfNeeded() {
        val key = draggingKey ?: return
        val dragged = draggingItemInfo ?: return
        // The list has not laid out the previous move yet; its positions are stale.
        if (dragged.index == indexAtLastMove) return
        val top = initialOffset + draggedDelta
        val middle = (top + dragged.size / 2f).toInt()
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            item.key != key && middle in item.offset..(item.offset + item.size) && canMove(key, item.key)
        } ?: return
        val firstIndex = listState.firstVisibleItemIndex
        val firstOffset = listState.firstVisibleItemScrollOffset
        indexAtLastMove = dragged.index
        onMove(key, target.key)
        // LazyColumn keeps its scroll anchored to the first visible key; when that item is
        // the one moving, pin the scroll position instead so the list does not jump.
        if (dragged.index == firstIndex || target.index == firstIndex) {
            scope.launch { listState.scrollToItem(firstIndex, firstOffset) }
        }
    }

    private suspend fun autoScrollLoop() {
        while (scope.isActive && draggingKey != null) {
            val dragged = draggingItemInfo
            val info = listState.layoutInfo
            if (dragged != null) {
                val top = initialOffset + draggedDelta
                val bottom = top + dragged.size
                val start = info.viewportStartOffset + info.beforeContentPadding
                val end = info.viewportEndOffset - info.afterContentPadding
                val speed = when {
                    top < start + autoScrollEdgePx -> -maxAutoScrollPx * ((start + autoScrollEdgePx - top) / autoScrollEdgePx).coerceIn(0f, 1f)
                    bottom > end - autoScrollEdgePx -> maxAutoScrollPx * ((bottom - (end - autoScrollEdgePx)) / autoScrollEdgePx).coerceIn(0f, 1f)
                    else -> 0f
                }
                if (speed != 0f) {
                    val consumed = listState.scrollBy(speed)
                    // Items moved under a stationary finger: re-check for a swap.
                    if (consumed != 0f) swapIfNeeded()
                }
            }
            withFrameNanos { }
        }
    }
    // ===== END SECTION: REORDERABLE_LIST_GESTURES =====

    private companion object {
        const val NO_INDEX = -1
    }
}

@Composable
fun rememberReorderableListState(
    listState: LazyListState,
    onMove: (fromKey: Any, toKey: Any) -> Unit,
    canMove: (fromKey: Any, toKey: Any) -> Boolean,
    onDragEnd: (key: Any) -> Unit,
): ReorderableListState {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val latestOnMove by rememberUpdatedState(onMove)
    val latestCanMove by rememberUpdatedState(canMove)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)
    return remember(listState, scope, density) {
        ReorderableListState(
            listState = listState,
            scope = scope,
            autoScrollEdgePx = with(density) { 72.dp.toPx() },
            maxAutoScrollPx = with(density) { 14.dp.toPx() },
            onMove = { from, to -> latestOnMove(from, to) },
            canMove = { from, to -> latestCanMove(from, to) },
            onDragEnd = { key -> latestOnDragEnd(key) },
        )
    }
}
// ===== END SECTION: REORDERABLE_LIST_STATE =====

// ===== SECTION: REORDERABLE_ITEM_MODIFIER =====
/** Starts dragging [key] after a long press; disabled when [enabled] is false. */
@Composable
fun Modifier.reorderableItem(state: ReorderableListState, key: Any, enabled: Boolean): Modifier {
    if (!enabled) return this
    val haptics = LocalHapticFeedback.current
    return pointerInput(state, key) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                state.start(key)
            },
            onDrag = { change, amount ->
                change.consume()
                state.drag(amount.y)
            },
            onDragEnd = { state.end() },
            onDragCancel = { state.end() },
        )
    }
}
// ===== END SECTION: REORDERABLE_ITEM_MODIFIER =====
