package com.kaushalkr.axiom.feature.academics.subject.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.GlassCard
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.model.Chapter
import com.kaushalkr.axiom.feature.tasks.components.TaskCompletionCheck

// ===== SECTION: CHAPTER_ROW =====
/**
 * One syllabus chapter on lightweight glass. The check marks it covered; screen readers also
 * get Move up, Move down and Delete actions.
 */
@Composable
fun ChapterRow(
    chapter: Chapter,
    ringColor: Color,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onMove: (offset: Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val moveUp = stringResource(R.string.task_action_move_up)
    val moveDown = stringResource(R.string.task_action_move_down)
    val delete = stringResource(R.string.task_action_delete)
    val actions = remember(chapter.id, canMoveUp, canMoveDown, moveUp, moveDown, delete) {
        buildList {
            if (canMoveUp) add(CustomAccessibilityAction(moveUp) { onMove(-1); true })
            if (canMoveDown) add(CustomAccessibilityAction(moveDown) { onMove(1); true })
            add(CustomAccessibilityAction(delete) { onDelete(); true })
        }
    }
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { customActions = actions },
        shape = GlassShapes.Tile,
        spec = GlassTokens.ListItem,
        contentPadding = PaddingValues(start = AxiomSpacing.xxs, end = AxiomSpacing.xxs),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TaskCompletionCheck(
                isCompleted = chapter.isDone,
                ringColor = ringColor,
                contentDescription = stringResource(
                    if (chapter.isDone) R.string.task_checkbox_reopen else R.string.task_checkbox_complete,
                    chapter.title,
                ),
                onToggle = onToggle,
            )
            Text(
                text = chapter.title,
                style = AxiomTheme.typography.bodyLarge.copy(
                    textDecoration = if (chapter.isDone) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = if (chapter.isDone) colors.textSecondary else colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            val removeLabel = stringResource(R.string.editor_subtask_remove, chapter.title)
            Box(
                modifier = Modifier
                    .size(AxiomDimens.minTouchTarget)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = removeLabel, onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(R.drawable.ic_close), contentDescription = removeLabel, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
// ===== END SECTION: CHAPTER_ROW =====
