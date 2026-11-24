package com.kaushalkr.axiom.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.kaushalkr.axiom.R
import kotlin.reflect.KClass

// ===== SECTION: NAV_TOP_LEVEL_DESTINATION =====
/** The five primary tabs, in navigation bar order. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    HOME(HomeRoute, HomeRoute::class, R.string.nav_home, R.drawable.ic_nav_home),
    PLANNER(PlannerRoute, PlannerRoute::class, R.string.nav_planner, R.drawable.ic_nav_planner),
    FOCUS(FocusRoute, FocusRoute::class, R.string.nav_focus, R.drawable.ic_nav_focus),
    INSIGHTS(InsightsRoute, InsightsRoute::class, R.string.nav_insights, R.drawable.ic_nav_insights),
    ASSISTANT(AssistantRoute, AssistantRoute::class, R.string.nav_assistant, R.drawable.ic_nav_assistant),
    ;

    companion object {
        /** The tab whose route is in [destination]'s hierarchy, or null for secondary screens. */
        fun from(destination: NavDestination?): TopLevelDestination? {
            if (destination == null) return null
            return entries.firstOrNull { topLevel ->
                destination.hierarchy.any { it.hasRoute(topLevel.routeClass) }
            }
        }
    }
}
// ===== END SECTION: NAV_TOP_LEVEL_DESTINATION =====
