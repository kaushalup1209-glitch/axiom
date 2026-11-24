package com.kaushalkr.axiom.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
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
 * With glass off, neither backdrop is captured.
 */
@Composable
fun AxiomApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val glassEnabled = AxiomTheme.glass.isEnabled
    val backgroundBackdrop = rememberLayerBackdrop()
    val screenBackdrop = rememberLayerBackdrop()
    val overlayHostState = remember { GlassOverlayHostState() }

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
                    .glassBackdropSource(screenBackdrop, glassEnabled)
                    .then(underlyingSemantics),
            ) {
                AxiomBackground(
                    Modifier
                        .fillMaxSize()
                        .glassBackdropSource(backgroundBackdrop, glassEnabled),
                )
                CompositionLocalProvider(
                    LocalGlassBackdrop provides backdropOrEmpty(backgroundBackdrop, glassEnabled),
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
                LocalGlassBackdrop provides backdropOrEmpty(screenBackdrop, glassEnabled),
            ) {
                AxiomTopActions(
                    isTopLevel = isTopLevel,
                    onOpenDistractionShield = { navController.navigateToDistractionShield() },
                    onOpenSettings = { navController.navigateToSettings() },
                    onNavigateBack = { navController.navigateUp() },
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
                    enter = slideInVertically(spring(dampingRatio = 0.8f, stiffness = 500f)) { it } +
                        fadeIn(tween(200)),
                    exit = slideOutVertically(tween(220)) { it } + fadeOut(tween(160)),
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
