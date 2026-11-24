package com.kaushalkr.axiom.feature.tasks.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.VisibilityThreshold
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotion
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.ui.reorder.ReorderableListState
import com.kaushalkr.axiom.core.ui.reorder.rememberReorderableListState
import com.kaushalkr.axiom.core.ui.reorder.reorderableItem
import com.kaushalkr.axiom.feature.tasks.TaskListItem
import com.kaushalkr.axiom.feature.tasks.TaskListReorder
import com.kaushalkr.axiom.feature.tasks.TaskSection
import com.kaushalkr.axiom.feature.tasks.TaskSectionKind
import com.kaushalkr.axiom.feature.tasks.TasksUiState
import com.kaushalkr.axiom.feature.tasks.rememberLongDateFormatter
import com.kaushalkr.axiom.feature.tasks.sectionTitle

// ===== SECTION: TASK_LIST =====
/**
 * The scrolling task list. Rows are keyed by task id, so completing, deleting or reordering
 * animates only the rows that change. While a row is dragged, a local copy of the order is
 * shown; the saved order replaces it once the move is written.
 */
@Composable
fun TaskList(
    state: TasksUiState,
    onOpenTask: (Long) -> Unit,
    onToggleCompleted: (TaskListItem) -> Unit,
    onDelete: (TaskListItem) -> Unit,
    onCommitMove: (item: TaskListItem, previous: TaskListItem?, next: TaskListItem?) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val motion = LocalAxiomMotion.current
    val listState = rememberLazyListState()

    // ===== SECTION: TASK_LIST_VIEW_RESET =====
    // A different view starts at the top; returning to this screen keeps its scroll position.
    var shownView by rememberSaveable { mutableStateOf(state.view) }
    LaunchedEffect(state.view) {
        if (state.view != shownView) {
            shownView = state.view
            listState.scrollToItem(0)
        }
    }
    // ===== END SECTION: TASK_LIST_VIEW_RESET =====

    // ===== SECTION: TASK_LIST_REORDER_STATE =====
    var dragSections by remember { mutableStateOf<List<TaskSection>?>(null) }
    val sections = dragSections ?: state.sections
    val reorderState = rememberReorderableListState(
        listState = listState,
        onMove = { from, to ->
            if (from is Long && to is Long) dragSections = TaskListReorder.move(dragSections ?: state.sections, from, to)
        },
        canMove = { from, to ->
            from is Long && to is Long && TaskListReorder.inSameSection(dragSections ?: state.sections, from, to)
        },
        onDragEnd = { key ->
            val moved = dragSections
            val placement = if (key is Long && moved != null) TaskListReorder.placementOf(moved, key) else null
            val original = if (key is Long) TaskListReorder.placementOf(state.sections, key) else null
            if (placement == null || placement.index == original?.index) {
                dragSections = null
            } else {
                onCommitMove(placement.item, placement.previous, placement.next)
            }
        },
    )
    // The saved order has arrived; drop the local copy (but never mid-drag).
    LaunchedEffect(state.sections) {
        if (reorderState.draggingKey == null) dragSections = null
    }
    // ===== END SECTION: TASK_LIST_REORDER_STATE =====

    val longDateFormatter = rememberLongDateFormatter()
    val isFiltered = state.isSearching || state.filter.isActive
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        if (state.isEmpty) {
            item(key = EMPTY_KEY, contentType = EMPTY_KEY) {
                TasksEmptyState(view = state.view, isFiltered = isFiltered, modifier = Modifier.listAnimation(this, motion, dragging = false))
            }
        }
        sections.forEach { section ->
            if (section.items.isEmpty()) return@forEach
            if (section.kind != TaskSectionKind.NONE) {
                item(key = "header-${section.key}", contentType = HEADER_CONTENT_TYPE) {
                    TaskSectionHeader(
                        title = sectionTitle(section, longDateFormatter).orEmpty(),
                        count = section.items.size,
                        modifier = Modifier.listAnimation(this, motion, dragging = false),
                    )
                }
            }
            items(items = section.items, key = { it.id }, contentType = { TASK_CONTENT_TYPE }) { item ->
                TaskRow(
                    item = item,
                    state = state,
                    sections = sections,
                    reorderState = reorderState,
                    motion = motion,
                    onOpenTask = onOpenTask,
                    onToggleCompleted = onToggleCompleted,
                    onDelete = onDelete,
                    onCommitMove = onCommitMove,
                )
            }
        }
    }
}
// ===== END SECTION: TASK_LIST =====

// ===== SECTION: TASK_LIST_ROW =====
@Composable
private fun LazyItemScope.TaskRow(
    item: TaskListItem,
    state: TasksUiState,
    sections: List<TaskSection>,
    reorderState: ReorderableListState,
    motion: AxiomMotion,
    onOpenTask: (Long) -> Unit,
    onToggleCompleted: (TaskListItem) -> Unit,
    onDelete: (TaskListItem) -> Unit,
    onCommitMove: (item: TaskListItem, previous: TaskListItem?, next: TaskListItem?) -> Unit,
) {
    val isDragging = reorderState.isDragging(item.id)
    val lift by animateFloatAsState(
        targetValue = if (isDragging) AxiomMotionTokens.DRAG_LIFT_SCALE else 1f,
        animationSpec = motion.spring(AxiomMotionTokens.DragLift),
        label = "TaskDragLift",
    )
    SwipeableTaskItem(
        item = item,
        today = state.today,
        showDragHandle = state.canReorder,
        onOpen = { onOpenTask(item.id) },
        onToggleCompleted = { onToggleCompleted(item) },
        onDelete = { onDelete(item) },
        accessibilityActions = taskAccessibilityActions(item, sections, state.canReorder, onToggleCompleted, onDelete, onCommitMove),
        swipeEnabled = !isDragging,
        modifier = Modifier.listAnimation(this, motion, dragging = isDragging)
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = reorderState.offsetOf(item.id)
                scaleX = lift
                scaleY = lift
            }
            .reorderableItem(reorderState, item.id, enabled = state.canReorder),
    )
}

/** Fade in and out, and slide to new positions; the dragged row follows the finger instead. */
private fun Modifier.listAnimation(scope: LazyItemScope, motion: AxiomMotion, dragging: Boolean): Modifier =
    if (!motion.isEnabled) {
        this
    } else {
        with(scope) {
            animateItem(
                fadeInSpec = motion.tween(AxiomMotionTokens.ListFadeIn),
                placementSpec = if (dragging) null else motion.spring(AxiomMotionTokens.ListPlacement, IntOffset.VisibilityThreshold),
                fadeOutSpec = motion.tween(AxiomMotionTokens.ListFadeOut),
            )
        }
    }
// ===== END SECTION: TASK_LIST_ROW =====

// ===== SECTION: TASK_LIST_ACCESSIBILITY =====
/** Screen-reader actions that mirror the swipe and drag gestures. */
@Composable
private fun taskAccessibilityActions(
    item: TaskListItem,
    sections: List<TaskSection>,
    canReorder: Boolean,
    onToggleCompleted: (TaskListItem) -> Unit,
    onDelete: (TaskListItem) -> Unit,
    onCommitMove: (item: TaskListItem, previous: TaskListItem?, next: TaskListItem?) -> Unit,
): List<CustomAccessibilityAction> {
    val toggleLabel = stringResource(if (item.isCompleted) R.string.task_action_reopen else R.string.task_action_complete)
    val deleteLabel = stringResource(R.string.task_action_delete)
    val moveUpLabel = stringResource(R.string.task_action_move_up)
    val moveDownLabel = stringResource(R.string.task_action_move_down)
    val moveUp = if (canReorder) TaskListReorder.shifted(sections, item.id, -1) else null
    val moveDown = if (canReorder) TaskListReorder.shifted(sections, item.id, 1) else null
    return remember(item, moveUp, moveDown, toggleLabel, deleteLabel, moveUpLabel, moveDownLabel) {
        buildList {
            add(CustomAccessibilityAction(toggleLabel) { onToggleCompleted(item); true })
            add(CustomAccessibilityAction(deleteLabel) { onDelete(item); true })
            moveUp?.let { placement ->
                add(CustomAccessibilityAction(moveUpLabel) { onCommitMove(placement.item, placement.previous, placement.next); true })
            }
            moveDown?.let { placement ->
                add(CustomAccessibilityAction(moveDownLabel) { onCommitMove(placement.item, placement.previous, placement.next); true })
            }
        }
    }
}
// ===== END SECTION: TASK_LIST_ACCESSIBILITY =====

private const val EMPTY_KEY = "empty"
private const val HEADER_CONTENT_TYPE = "header"
private const val TASK_CONTENT_TYPE = "task"
