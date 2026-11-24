package com.kaushalkr.axiom.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: AXIOM_CHIP =====
/**
 * Selectable option pill for use inside glass panels. Deliberately not glass, so a row of
 * chips adds no backdrop layers; the panel around it carries the glass.
 *
 * @param dotColor optional leading colour dot (e.g. a priority colour).
 */
@Composable
fun AxiomChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    dotColor: Color? = null,
    role: Role = Role.Checkbox,
) {
    val colors = AxiomTheme.colors
    val motion = LocalAxiomMotion.current
    val container by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.controlTrack,
        animationSpec = motion.tween(AxiomMotionTokens.ExpandFade),
        label = "ChipContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onAccent else colors.textPrimary,
        animationSpec = motion.tween(AxiomMotionTokens.ExpandFade),
        label = "ChipContent",
    )
    Row(
        modifier = modifier
            .clip(GlassShapes.Capsule)
            .background(container)
            .selectable(selected = selected, role = role, onClick = onClick)
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColor != null) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        }
        if (iconRes != null) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        }
        Text(text = label, style = AxiomTheme.typography.labelLarge, color = content, maxLines = 1)
    }
}
// ===== END SECTION: AXIOM_CHIP =====
