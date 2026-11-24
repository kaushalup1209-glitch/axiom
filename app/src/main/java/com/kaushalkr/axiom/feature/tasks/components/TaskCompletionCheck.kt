package com.kaushalkr.axiom.feature.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: TASK_COMPLETION_CHECK =====
/** Round completion check, ringed in the task's priority colour. 48dp touch target. */
@Composable
fun TaskCompletionCheck(
    isCompleted: Boolean,
    ringColor: Color,
    contentDescription: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AxiomTheme.colors
    val motion = LocalAxiomMotion.current
    val fill by animateColorAsState(
        targetValue = if (isCompleted) colors.accent else Color.Transparent,
        animationSpec = motion.tween(AxiomMotionTokens.ExpandFade),
        label = "CheckFill",
    )
    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0.4f,
        animationSpec = motion.spring(AxiomMotionTokens.PressIn),
        label = "CheckScale",
    )
    Box(
        modifier = modifier
            .size(AxiomDimens.minTouchTarget)
            .toggleable(value = isCompleted, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(fill)
                .border(2.dp, if (isCompleted) colors.accent else ringColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                        },
                )
            }
        }
    }
}
// ===== END SECTION: TASK_COMPLETION_CHECK =====
