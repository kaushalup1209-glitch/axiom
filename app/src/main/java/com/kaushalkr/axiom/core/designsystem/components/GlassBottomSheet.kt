package com.kaushalkr.axiom.core.designsystem.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.glass.GlassShapes
import com.kaushalkr.axiom.core.designsystem.glass.GlassTokens
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassStyle
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme

// ===== SECTION: GLASS_BOTTOM_SHEET =====
/**
 * Modal Liquid Glass sheet rising from the bottom, used for menus, filters and pickers.
 * Rendered above the whole app through [GlassOverlayHost], so its glass refracts the screen.
 * Keep [visible] in the caller (e.g. `rememberSaveable`).
 */
@Composable
fun GlassBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    closeLabel: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val host = LocalGlassOverlayHost.current
        ?: error("GlassBottomSheet requires a GlassOverlayHost provided by the app root.")
    val owner = remember { Any() }
    val latestTitle by rememberUpdatedState(title)
    val latestCloseLabel by rememberUpdatedState(closeLabel)
    val latestOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val latestContent by rememberUpdatedState(content)

    val sheet: @Composable () -> Unit = remember {
        {
            GlassBottomSheetLayout(
                title = latestTitle,
                closeLabel = latestCloseLabel,
                onDismissRequest = latestOnDismissRequest,
                content = latestContent,
            )
        }
    }
    DisposableEffect(host, visible) {
        if (visible) host.show(owner, sheet)
        onDispose { host.hide(owner) }
    }
}
// ===== END SECTION: GLASS_BOTTOM_SHEET =====

// ===== SECTION: GLASS_BOTTOM_SHEET_LAYOUT =====
@Composable
private fun GlassBottomSheetLayout(
    title: String,
    closeLabel: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AxiomTheme.colors
    BackHandler(onBack = onDismissRequest)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scrim)
            .pointerInput(Unit) { detectTapGestures { onDismissRequest() } }
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(AxiomSpacing.sm),
        contentAlignment = Alignment.BottomCenter,
    ) {
        GlassCard(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .heightIn(max = maxHeight * MAX_HEIGHT_FRACTION)
                .sheetEnterOffset()
                .semantics { paneTitle = title }
                .pointerInput(Unit) { detectTapGestures { } },
            shape = GlassShapes.Sheet,
            spec = GlassTokens.Sheet,
            surfaceColor = LocalGlassStyle.current.dialogSurfaceColor,
            contentPadding = PaddingValues(start = AxiomSpacing.lg, end = AxiomSpacing.lg, top = AxiomSpacing.sm, bottom = AxiomSpacing.lg),
            hostsGlass = true,
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(GlassShapes.Capsule)
                    .background(colors.controlTrack),
            )
            Spacer(Modifier.height(AxiomSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = AxiomTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                GlassIconButton(
                    onClick = onDismissRequest,
                    iconRes = R.drawable.ic_close,
                    contentDescription = closeLabel,
                    floating = false,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(AxiomSpacing.md))
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AxiomSpacing.md),
                content = content,
            )
        }
    }
}

/** Slides the sheet up from below, driven by the overlay host's enter/exit transition. */
@Composable
private fun Modifier.sheetEnterOffset(): Modifier {
    val scope = LocalAnimatedVisibilityScope.current ?: return this
    val motion = LocalAxiomMotion.current
    val hiddenFraction by scope.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) motion.spring(AxiomMotionTokens.SheetIn)
            else motion.tween(AxiomMotionTokens.SheetOut)
        },
        label = "GlassSheetOffset",
    ) { state -> if (state == EnterExitState.Visible) 0f else 1f }
    return graphicsLayer { translationY = hiddenFraction * size.height * SLIDE_FRACTION }
}

private const val MAX_HEIGHT_FRACTION = 0.85f
private const val SLIDE_FRACTION = 0.35f
// ===== END SECTION: GLASS_BOTTOM_SHEET_LAYOUT =====
