package com.kaushalkr.axiom.app

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.designsystem.components.GlassUndoSnackbarHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kaushalkr.axiom.core.designsystem.components.AxiomBackground
import com.kaushalkr.axiom.core.designsystem.components.GlassOverlayHost
import com.kaushalkr.axiom.core.designsystem.components.GlassOverlayHostState
import com.kaushalkr.axiom.core.designsystem.components.LocalGlassOverlayHost
import com.kaushalkr.axiom.core.designsystem.components.StatusBarScrim
import com.kaushalkr.axiom.core.designsystem.glass.LocalGlassBackdrop
import com.kaushalkr.axiom.core.designsystem.glass.backdropOrEmpty
import com.kaushalkr.axiom.core.designsystem.glass.glassBackdropSource
import com.kaushalkr.axiom.core.designsystem.layout.AxiomWidthClass
import com.kaushalkr.axiom.core.designsystem.layout.LocalAxiomWidthClass
import com.kaushalkr.axiom.core.designsystem.layout.LocalScreenContentPadding
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomDimens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.navigation.AxiomNavHost
import com.kaushalkr.axiom.navigation.AxiomNavigationBar
import com.kaushalkr.axiom.navigation.AxiomTopActions
import com.kaushalkr.axiom.navigation.TopLevelDestination
import com.kaushalkr.axiom.navigation.navigateToDistractionShield
import com.kaushalkr.axiom.navigation.navigateToSettings
import com.kaushalkr.axiom.navigation.navigateToTopLevel
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

// ===== SECTION: APP_ROOT =====
/**
 * Root composable. Layer order, bottom to top:
 *
 * 1. Captured screen: ambient background (captured as `backgroundBackdrop` for in-screen
 *    glass) plus the navigation host. The whole layer is captured as `screenBackdrop`.
 * 2. Status bar scrim.
 * 3. Floating chrome (top actions, navigation bar) and the overlay host, which draw
 *    `screenBackdrop` so they refract the content scrolling beneath them.
 *
 * Chrome lives outside the captured layer, which is what prevents a capture loop.
 * Full captures both backdrops; Lite captures only `screenBackdrop`, shared by its blurred
 * navigation bar, dialogs and sheets; Off captures neither.
 */
@Composable
fun AxiomApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val glass = AxiomTheme.glass
    // Full captures both layers; Lite only the screen (for its blurred navigation bar, dialogs
    // and sheets); Off captures nothing.
    val capturesBackground = glass.capturesBackgroundBackdrop
    val capturesScreen = glass.capturesScreenBackdrop
    val motion = AxiomTheme.motion
    val backgroundBackdrop = rememberLayerBackdrop()
    val screenBackdrop = rememberLayerBackdrop()
    val overlayHostState = remember { GlassOverlayHostState() }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current
    val undoController = remember(context) { (context.applicationContext as AxiomApplication).container.undoController }

    // ===== SECTION: APP_ROOT_NAV_STATE =====
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTopLevel = TopLevelDestination.from(backStackEntry?.destination)
    val isTopLevel = backStackEntry == null || currentTopLevel != null
    var lastTopLevel by rememberSaveable { mutableStateOf(TopLevelDestination.HOME) }
    SideEffect { if (currentTopLevel != null) lastTopLevel = currentTopLevel }
    val selectedTab = currentTopLevel ?: lastTopLevel
    // ===== END SECTION: APP_ROOT_NAV_STATE =====

    BoxWithConstraints(modifier.fillMaxSize()) {
        // ===== SECTION: APP_ROOT_INSETS =====
        val layoutDirection = LocalLayoutDirection.current
        val widthClass = AxiomWidthClass.fromWidth(maxWidth)
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val horizontalSafe = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
        val safeStart = horizontalSafe.calculateStartPadding(layoutDirection)
        val safeEnd = horizontalSafe.calculateEndPadding(layoutDirection)
        val bottomChrome = if (isTopLevel) {
            AxiomDimens.navigationBarHeight + AxiomDimens.navigationBarBottomGap
        } else {
            0.dp
        }
        val screenContentPadding = PaddingValues(
            start = safeStart,
            end = safeEnd,
            top = statusBarTop + AxiomDimens.topBarHeight + AxiomSpacing.md,
            bottom = navigationBarBottom + bottomChrome + AxiomSpacing.xxl,
        )
        // ===== END SECTION: APP_ROOT_INSETS =====

        val underlyingSemantics =
            if (overlayHostState.isShowing) Modifier.clearAndSetSemantics {} else Modifier

        CompositionLocalProvider(
            LocalAxiomWidthClass provides widthClass,
            LocalGlassOverlayHost provides overlayHostState,
        ) {
            // ===== SECTION: APP_ROOT_CAPTURED_SCREEN =====
            Box(
                Modifier
                    .fillMaxSize()
                    .glassBackdropSource(screenBackdrop, capturesScreen)
                    .then(underlyingSemantics),
            ) {
                AxiomBackground(
                    Modifier
                        .fillMaxSize()
                        .glassBackdropSource(backgroundBackdrop, capturesBackground),
                )
                CompositionLocalProvider(
                    LocalGlassBackdrop provides backdropOrEmpty(backgroundBackdrop, capturesBackground),
                    LocalScreenContentPadding provides screenContentPadding,
                ) {
                    AxiomNavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            // ===== END SECTION: APP_ROOT_CAPTURED_SCREEN =====

            StatusBarScrim(height = statusBarTop + AxiomSpacing.sm)

            // ===== SECTION: APP_ROOT_FLOATING_CHROME =====
            CompositionLocalProvider(
                LocalGlassBackdrop provides backdropOrEmpty(screenBackdrop, capturesScreen),
            ) {
                AxiomTopActions(
                    isTopLevel = isTopLevel,
                    onOpenDistractionShield = { navController.navigateToDistractionShield() },
                    onOpenSettings = { navController.navigateToSettings() },
                    // Through the back dispatcher, so screens can intercept it (e.g. unsaved edits).
                    onNavigateBack = { backDispatcher?.onBackPressed() ?: navController.navigateUp() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .then(underlyingSemantics)
                        .padding(top = statusBarTop, start = safeStart, end = safeEnd)
                        .widthIn(max = AxiomDimens.contentMaxWidth)
                        .padding(horizontal = AxiomDimens.screenHorizontalPadding - 4.dp),
                )

                AnimatedVisibility(
                    visible = isTopLevel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(underlyingSemantics),
                    enter = slideInVertically(motion.spring(AxiomMotionTokens.ChromeShow, IntOffset.VisibilityThreshold)) { it } +
                        fadeIn(motion.tween(AxiomMotionTokens.ChromeFadeIn)),
                    exit = slideOutVertically(motion.tween(AxiomMotionTokens.ChromeHide)) { it } +
                        fadeOut(motion.tween(AxiomMotionTokens.ChromeFadeOut)),
                ) {
                    AxiomNavigationBar(
                        selectedDestination = selectedTab,
                        onDestinationSelected = { destination -> navController.navigateToTopLevel(destination) },
                        modifier = Modifier
                            .padding(
                                start = safeStart + AxiomSpacing.md,
                                end = safeEnd + AxiomSpacing.md,
                                bottom = navigationBarBottom + AxiomDimens.navigationBarBottomGap,
                            )
                            .widthIn(max = AxiomDimens.navigationBarMaxWidth)
                            .fillMaxWidth(),
                    )
                }

                // ===== SECTION: APP_ROOT_UNDO_SNACKBAR =====
                val snackbarBottom by animateDpAsState(
                    targetValue = navigationBarBottom + if (isTopLevel) {
                        AxiomDimens.navigationBarHeight + AxiomDimens.navigationBarBottomGap + AxiomSpacing.sm
                    } else {
                        AxiomSpacing.md
                    },
                    animationSpec = motion.spring(AxiomMotionTokens.ChromeShow, Dp.VisibilityThreshold),
                    label = "SnackbarBottom",
                )
                GlassUndoSnackbarHost(
                    undoController = undoController,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .then(underlyingSemantics)
                        .padding(start = safeStart + AxiomSpacing.md, end = safeEnd + AxiomSpacing.md)
                        .padding(bottom = snackbarBottom)
                        .imePadding(),
                )
                // ===== END SECTION: APP_ROOT_UNDO_SNACKBAR =====

                GlassOverlayHost(
                    state = overlayHostState,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            // ===== END SECTION: APP_ROOT_FLOATING_CHROME =====
        }
    }
}
// ===== END SECTION: APP_ROOT =====
