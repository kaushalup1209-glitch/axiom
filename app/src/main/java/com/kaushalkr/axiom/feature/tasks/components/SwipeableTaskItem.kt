package com.kaushalkr.axiom.feature.tasks.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.tasks.TaskListItem
import kotlinx.coroutines.delay
import java.time.LocalDate

// ===== SECTION: SWIPEABLE_TASK_ITEM =====
/**
 * [TaskCard] with swipe actions: swipe right to complete (or reopen a done task), swipe left
 * to delete. Both are undoable from the snackbar. The coloured action strip only draws while
 * swiping, so it never shows through the glass at rest.
 */
@Composable
fun SwipeableTaskItem(
    item: TaskListItem,
    today: LocalDate,
    showDragHandle: Boolean,
    onOpen: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    accessibilityActions: List<CustomAccessibilityAction>,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
) {
    // Plain remember, not rememberSaveable: the list saves item state by task id, and a restored
    // "dismissed" state would dismiss the row again the moment undo brings the task back.
    val state = remember {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = { distance -> distance * SWIPE_THRESHOLD_FRACTION },
        )
    }
    // A swiped row normally leaves the list. If it is still here after its exit (for example,
    // undo arrived mid-animation), slide it back instead of leaving an empty gap.
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissBoxValue.Settled) {
            delay(SWIPE_RESET_DELAY_MILLIS)
            state.reset()
        }
    }
    // SwipeToDismissBox re-runs onDismiss whenever the callback instance changes, so keep one
    // stable callback that reads the latest actions.
    val latestToggle by rememberUpdatedState(onToggleCompleted)
    val latestDelete by rememberUpdatedState(onDelete)
    val handleDismiss: (SwipeToDismissBoxValue) -> Unit = remember {
        { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> latestToggle()
                SwipeToDismissBoxValue.EndToStart -> latestDelete()
                SwipeToDismissBoxValue.Settled -> Unit
            }
        }
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        gesturesEnabled = swipeEnabled,
        backgroundContent = {
            val direction = state.dismissDirection
            if (direction != SwipeToDismissBoxValue.Settled) {
                val isComplete = direction == SwipeToDismissBoxValue.StartToEnd
                SwipeActionBackground(
                    alignEnd = !isComplete,
                    color = if (isComplete) AxiomTheme.colors.success else AxiomTheme.colors.danger,
                    iconRes = if (!isComplete) R.drawable.ic_delete else if (item.isCompleted) R.drawable.ic_undo else R.drawable.ic_check,
                    label = stringResource(
                        when {
                            !isComplete -> R.string.task_action_delete
                            item.isCompleted -> R.string.task_action_reopen
                            else -> R.string.task_action_complete
                        },
                    ),
                    progress = { state.progress },
                )
            }
        },
        onDismiss = handleDismiss,
    ) {
        TaskCard(
            item = item,
            today = today,
            showDragHandle = showDragHandle,
            onOpen = onOpen,
            onToggleCompleted = onToggleCompleted,
            accessibilityActions = accessibilityActions,
        )
    }
}

private const val SWIPE_THRESHOLD_FRACTION = 0.35f
private const val SWIPE_RESET_DELAY_MILLIS = 700L
// ===== END SECTION: SWIPEABLE_TASK_ITEM =====

// ===== SECTION: SWIPE_ACTION_BACKGROUND =====
@Composable
private fun SwipeActionBackground(
    alignEnd: Boolean,
    color: Color,
    @DrawableRes iconRes: Int,
    label: String,
    progress: () -> Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = (progress() * 2.5f).coerceIn(0f, 1f) }
            .clip(GlassShapes.Tile)
            .background(color)
            .padding(horizontal = AxiomSpacing.lg),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.xs)) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Text(text = label, style = AxiomTheme.typography.labelLarge, color = Color.White)
        }
    }
}
// ===== END SECTION: SWIPE_ACTION_BACKGROUND =====
