package com.kaushalkr.axiom.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotion
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.feature.assistant.AssistantScreen
import com.kaushalkr.axiom.feature.focus.FocusScreen
import com.kaushalkr.axiom.feature.home.HomeDashboardRoute
import com.kaushalkr.axiom.feature.insights.InsightsScreen
import com.kaushalkr.axiom.feature.planner.PlannerScreen
import com.kaushalkr.axiom.feature.settings.SettingsRoute
import com.kaushalkr.axiom.feature.shield.DistractionShieldScreen

// ===== SECTION: NAV_HOST =====
/**
 * App navigation graph. Tabs cross-fade with a slight scale; secondary screens slide in on a
 * spring from the trailing edge while the screen underneath recedes and fades. All timing
 * comes from [AxiomMotionTokens] at the user's animation scale.
 */
@Composable
fun AxiomNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val motion = LocalAxiomMotion.current
    val transitions = remember(motion) { AxiomNavTransitions(motion) }

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = {
            if (targetState.isSecondary()) transitions.pushEnter(this) else transitions.tabEnter
        },
        exitTransition = {
            if (targetState.isSecondary()) transitions.underlayExit else transitions.tabExit
        },
        popEnterTransition = {
            if (initialState.isSecondary()) transitions.underlayEnter else transitions.tabEnter
        },
        popExitTransition = {
            if (initialState.isSecondary()) transitions.popExit(this) else transitions.tabExit
        },
    ) {
        // ===== SECTION: NAV_HOST_TOP_LEVEL =====
        composable<HomeRoute> {
            HomeDashboardRoute(onOpenDistractionShield = { navController.navigateToDistractionShield() })
        }
        composable<PlannerRoute> { PlannerScreen() }
        composable<FocusRoute> { FocusScreen() }
        composable<InsightsRoute> { InsightsScreen() }
        composable<AssistantRoute> { AssistantScreen() }
        // ===== END SECTION: NAV_HOST_TOP_LEVEL =====

        // ===== SECTION: NAV_HOST_SECONDARY =====
        composable<DistractionShieldRoute> { DistractionShieldScreen() }
        composable<SettingsRoute> { SettingsRoute() }
        // ===== END SECTION: NAV_HOST_SECONDARY =====
    }
}

private fun NavBackStackEntry.isSecondary(): Boolean = TopLevelDestination.from(destination) == null
// ===== END SECTION: NAV_HOST =====

// ===== SECTION: NAV_TRANSITIONS =====
/** Screen transitions built from motion tokens for one animation scale. */
private class AxiomNavTransitions(private val motion: AxiomMotion) {

    val tabEnter: EnterTransition =
        fadeIn(motion.tween(AxiomMotionTokens.TabFadeIn)) +
            scaleIn(
                animationSpec = motion.tween(AxiomMotionTokens.TabFadeIn),
                initialScale = AxiomMotionTokens.TAB_ENTER_SCALE,
            )

    val tabExit: ExitTransition = fadeOut(motion.tween(AxiomMotionTokens.TabFadeOut))

    /** The screen left underneath a pushed secondary screen. */
    val underlayExit: ExitTransition =
        fadeOut(motion.tween(AxiomMotionTokens.PushFadeOut)) +
            scaleOut(
                animationSpec = motion.tween(AxiomMotionTokens.PushFadeOut),
                targetScale = AxiomMotionTokens.PUSH_UNDERLAY_SCALE,
            )

    /** The screen returning from underneath when a secondary screen is popped. */
    val underlayEnter: EnterTransition =
        fadeIn(motion.tween(AxiomMotionTokens.PushFadeIn)) +
            scaleIn(
                animationSpec = motion.tween(AxiomMotionTokens.PushFadeIn),
                initialScale = AxiomMotionTokens.PUSH_UNDERLAY_SCALE,
            )

    fun pushEnter(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition =
        scope.slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = motion.spring(AxiomMotionTokens.PushSlide, IntOffset.VisibilityThreshold),
            initialOffset = { fullWidth -> (fullWidth * AxiomMotionTokens.PUSH_OFFSET_FRACTION).toInt() },
        ) + fadeIn(motion.tween(AxiomMotionTokens.PushFadeIn))

    fun popExit(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition =
        scope.slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = motion.spring(AxiomMotionTokens.PushSlide, IntOffset.VisibilityThreshold),
            targetOffset = { fullWidth -> (fullWidth * AxiomMotionTokens.PUSH_OFFSET_FRACTION).toInt() },
        ) + fadeOut(motion.tween(AxiomMotionTokens.PushFadeOut))
}
// ===== END SECTION: NAV_TRANSITIONS =====

// ===== SECTION: NAV_CONTROLLER_EXTENSIONS =====
/** Switches tabs with saved per-tab state and no duplicate back stack entries. */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigateToSettings() {
    navigate(SettingsRoute) { launchSingleTop = true }
}

fun NavHostController.navigateToDistractionShield() {
    navigate(DistractionShieldRoute) { launchSingleTop = true }
}
// ===== END SECTION: NAV_CONTROLLER_EXTENSIONS =====
