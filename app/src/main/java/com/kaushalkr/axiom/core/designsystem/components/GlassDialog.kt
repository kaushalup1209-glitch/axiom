package com.kaushalkr.axiom.core.designsystem.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kyant.shapes.RoundedRectangle

// ===== SECTION: GLASS_DIALOG =====
/**
 * Modal confirmation dialog rendered as a Liquid Glass panel above the whole app.
 *
 * Keep [visible] in the caller (for example with `rememberSaveable`) so the dialog
 * survives configuration changes. Requires a [GlassOverlayHost] at the app root.
 *
 * @param dismissLabel when null, only the confirm button is shown.
 * @param isDestructive colours the confirm button with the danger colour.
 */
@Composable
fun GlassDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String? = null,
    isDestructive: Boolean = false,
) {
    val host = LocalGlassOverlayHost.current
        ?: error("GlassDialog requires a GlassOverlayHost provided by the app root.")
    val owner = remember { Any() }

    val latestTitle by rememberUpdatedState(title)
    val latestMessage by rememberUpdatedState(message)
    val latestConfirmLabel by rememberUpdatedState(confirmLabel)
    val latestDismissLabel by rememberUpdatedState(dismissLabel)
    val latestIsDestructive by rememberUpdatedState(isDestructive)
    val latestOnConfirm by rememberUpdatedState(onConfirm)
    val latestOnDismissRequest by rememberUpdatedState(onDismissRequest)

    val dialogContent: @Composable () -> Unit = remember {
        {
            GlassDialogLayout(
                title = latestTitle,
                message = latestMessage,
                confirmLabel = latestConfirmLabel,
                dismissLabel = latestDismissLabel,
                isDestructive = latestIsDestructive,
                onConfirm = latestOnConfirm,
                onDismissRequest = latestOnDismissRequest,
            )
        }
    }

    DisposableEffect(host, visible) {
        if (visible) host.show(owner, dialogContent)
        onDispose { host.hide(owner) }
    }
}
// ===== END SECTION: GLASS_DIALOG =====

// ===== SECTION: GLASS_DIALOG_LAYOUT =====
@Composable
private fun GlassDialogLayout(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String?,
    isDestructive: Boolean,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val colors = AxiomTheme.colors

    BackHandler(onBack = onDismissRequest)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scrim)
            .pointerInput(Unit) { detectTapGestures { onDismissRequest() } }
            .safeDrawingPadding()
            .padding(AxiomSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        GlassCard(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .dialogEnterScale()
                .semantics { paneTitle = title }
                // Consume taps so touching the panel does not dismiss the dialog.
                .pointerInput(Unit) { detectTapGestures { } },
            shape = RoundedRectangle(AxiomDimens.dialogCornerRadius),
            spec = GlassTokens.Dialog,
            surfaceColor = LocalGlassStyle.current.dialogSurfaceColor,
            contentPadding = PaddingValues(
                start = AxiomSpacing.xl,
                end = AxiomSpacing.xl,
                top = AxiomSpacing.xl,
                bottom = AxiomSpacing.lg,
            ),
        ) {
            Text(
                text = title,
                style = AxiomTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(AxiomSpacing.sm))
            Text(
                text = message,
                style = AxiomTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(AxiomSpacing.xl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.sm),
            ) {
                if (dismissLabel != null) {
                    GlassButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(dismissLabel)
                    }
                }
                GlassButton(
                    onClick = {
                        onConfirm()
                        onDismissRequest()
                    },
                    modifier = Modifier.weight(1f),
                    tint = if (isDestructive) colors.danger else colors.accent,
                    contentColor = if (isDestructive) Color.White else colors.onAccent,
                ) {
                    Text(confirmLabel)
                }
            }
        }
    }
}
// ===== END SECTION: GLASS_DIALOG_LAYOUT =====

// ===== SECTION: GLASS_DIALOG_ENTER_SCALE =====
/** Springy scale-in for the panel, driven by the surrounding overlay visibility transition. */
@Composable
private fun Modifier.dialogEnterScale(): Modifier {
    val scope = LocalAnimatedVisibilityScope.current ?: return this
    val motion = LocalAxiomMotion.current
    val scale by scope.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) motion.spring(AxiomMotionTokens.DialogIn)
            else motion.tween(AxiomMotionTokens.DialogOut)
        },
        label = "GlassDialogScale",
    ) { state -> if (state == EnterExitState.Visible) 1f else AxiomMotionTokens.DIALOG_HIDDEN_SCALE }
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
// ===== END SECTION: GLASS_DIALOG_ENTER_SCALE =====
