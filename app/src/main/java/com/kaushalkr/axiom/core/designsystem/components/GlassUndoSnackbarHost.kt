package com.kaushalkr.axiom.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.VisibilityThreshold
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.glass.glassSurface
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.core.domain.UndoController
import com.kaushalkr.axiom.core.domain.UndoEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

// ===== SECTION: GLASS_UNDO_SNACKBAR_HOST =====
/**
 * Shows the latest [UndoEvent] as a floating Liquid Glass snackbar with an Undo action.
 * Lives in the app's floating chrome layer, so its glass refracts the content beneath it.
 * A new event replaces the current one; each stays for the accessibility-recommended time.
 */
@Composable
fun GlassUndoSnackbarHost(
    undoController: UndoController,
    modifier: Modifier = Modifier,
) {
    val motion = LocalAxiomMotion.current
    val accessibilityManager = LocalAccessibilityManager.current
    var current by remember { mutableStateOf<UndoEvent?>(null) }
    val lastShown = remember { arrayOfNulls<UndoEvent>(1) }
    if (current != null) lastShown[0] = current

    LaunchedEffect(undoController) {
        undoController.events.collectLatest { event ->
            current = event
            val timeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
                originalTimeoutMillis = AxiomMotionTokens.SNACKBAR_DURATION_MILLIS,
                containsIcons = true,
                containsText = true,
                containsControls = true,
            ) ?: AxiomMotionTokens.SNACKBAR_DURATION_MILLIS
            delay(timeout)
            if (current === event) current = null
        }
    }

    AnimatedVisibility(
        visible = current != null,
        modifier = modifier,
        enter = slideInVertically(motion.spring(AxiomMotionTokens.SnackbarIn, IntOffset.VisibilityThreshold)) { it } +
            fadeIn(motion.tween(AxiomMotionTokens.ChromeFadeIn)),
        exit = slideOutVertically(motion.tween(AxiomMotionTokens.SnackbarOut)) { it / 2 } +
            fadeOut(motion.tween(AxiomMotionTokens.SnackbarOut)),
    ) {
        val event = current ?: lastShown[0] ?: return@AnimatedVisibility
        GlassSnackbar(
            message = stringResource(event.messageRes, event.messageArgument.orEmpty()),
            onUndo = {
                undoController.undo(event)
                current = null
            },
        )
    }
}
// ===== END SECTION: GLASS_UNDO_SNACKBAR_HOST =====

// ===== SECTION: GLASS_SNACKBAR =====
@Composable
private fun GlassSnackbar(
    message: String,
    onUndo: () -> Unit,
) {
    val colors = AxiomTheme.colors
    val style = LocalGlassStyle.current
    val backdrop = LocalGlassBackdrop.current
    val surface = remember(backdrop, style) {
        Modifier.glassSurface(
            backdrop = backdrop,
            style = style,
            spec = GlassTokens.Snackbar,
            shape = GlassShapes.Capsule,
            surfaceColor = style.dialogSurfaceColor,
        )
    }
    val undoLabel = stringResource(R.string.action_undo)
    Row(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .then(surface)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
            .padding(start = AxiomSpacing.lg, end = AxiomSpacing.xs, top = AxiomSpacing.xxs, bottom = AxiomSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
    ) {
        Text(
            text = message,
            style = AxiomTheme.typography.bodyMedium,
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .clip(GlassShapes.Capsule)
                .clickable(role = Role.Button, onClick = onUndo)
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = AxiomSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_undo), contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
            Text(text = undoLabel, style = AxiomTheme.typography.labelLarge, color = colors.accent)
        }
    }
}
// ===== END SECTION: GLASS_SNACKBAR =====
