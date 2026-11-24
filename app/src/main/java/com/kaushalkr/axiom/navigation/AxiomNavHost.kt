package com.kaushalkr.axiom.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kaushalkr.axiom.feature.assistant.AssistantScreen
import com.kaushalkr.axiom.feature.focus.FocusScreen
import com.kaushalkr.axiom.feature.home.HomeScreen
import com.kaushalkr.axiom.feature.insights.InsightsScreen
import com.kaushalkr.axiom.feature.planner.PlannerScreen
import com.kaushalkr.axiom.feature.settings.SettingsRoute
import com.kaushalkr.axiom.feature.shield.DistractionShieldScreen

// ===== SECTION: NAV_HOST =====
/** App navigation graph. Tabs cross-fade; secondary screens slide in from the trailing edge. */
@Composable
fun AxiomNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { TabEnterTransition },
        exitTransition = { TabExitTransition },
        popEnterTransition = { TabEnterTransition },
        popExitTransition = { TabExitTransition },
    ) {
        // ===== SECTION: NAV_HOST_TOP_LEVEL =====
        composable<HomeRoute> { HomeScreen() }
        composable<PlannerRoute> { PlannerScreen() }
        composable<FocusRoute> { FocusScreen() }
        composable<InsightsRoute> { InsightsScreen() }
        composable<AssistantRoute> { AssistantScreen() }
        // ===== END SECTION: NAV_HOST_TOP_LEVEL =====

        // ===== SECTION: NAV_HOST_SECONDARY =====
        composable<DistractionShieldRoute>(
            enterTransition = { secondaryEnter() },
            popExitTransition = { secondaryPopExit() },
        ) { DistractionShieldScreen() }
        composable<SettingsRoute>(
            enterTransition = { secondaryEnter() },
            popExitTransition = { secondaryPopExit() },
        ) { SettingsRoute() }
        // ===== END SECTION: NAV_HOST_SECONDARY =====
    }
}
// ===== END SECTION: NAV_HOST =====

// ===== SECTION: NAV_TRANSITIONS =====
private val TabEnterTransition: EnterTransition =
    fadeIn(tween(durationMillis = 220, delayMillis = 60)) +
        scaleIn(initialScale = 0.985f, animationSpec = tween(durationMillis = 260, delayMillis = 60))

private val TabExitTransition: ExitTransition =
    fadeOut(tween(durationMillis = 120))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.secondaryEnter(): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(durationMillis = 320),
        initialOffset = { fullWidth -> fullWidth / 3 },
    ) + fadeIn(tween(durationMillis = 240))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.secondaryPopExit(): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(durationMillis = 280),
        targetOffset = { fullWidth -> fullWidth / 3 },
    ) + fadeOut(tween(durationMillis = 200))
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
