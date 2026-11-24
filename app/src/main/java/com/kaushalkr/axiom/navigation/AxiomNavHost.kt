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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotion
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.motion.LocalAxiomMotion
import com.kaushalkr.axiom.feature.academics.exam.ExamEditorRoute
import com.kaushalkr.axiom.feature.academics.subject.SubjectDetailRoute
import com.kaushalkr.axiom.feature.assistant.AssistantScreen
import com.kaushalkr.axiom.feature.focus.FocusScreen
import com.kaushalkr.axiom.feature.home.HomeDashboardRoute
import com.kaushalkr.axiom.feature.insights.InsightsScreen
import com.kaushalkr.axiom.feature.planner.PlannerScreen
import com.kaushalkr.axiom.feature.planner.PlannerSection
import com.kaushalkr.axiom.feature.routine.editor.RoutineBlockEditorRoute
import com.kaushalkr.axiom.feature.settings.SettingsRoute
import com.kaushalkr.axiom.feature.shield.DistractionShieldScreen
import com.kaushalkr.axiom.feature.tasks.editor.TaskEditorScreenRoute
import java.time.LocalDate

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
    // A Planner section another screen asked to open; PlannerScreen applies it once, then clears it.
    var requestedPlannerSection by rememberSaveable { mutableStateOf<PlannerSection?>(null) }
    val openPlannerSection: (PlannerSection) -> Unit = { section ->
        requestedPlannerSection = section
        navController.navigateToTopLevel(TopLevelDestination.PLANNER)
    }

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
            HomeDashboardRoute(
                onOpenDistractionShield = { navController.navigateToDistractionShield() },
                onOpenPlanner = { openPlannerSection(PlannerSection.TASKS) },
                onOpenTask = { taskId -> navController.navigateToTaskEditor(taskId) },
                onAddTask = { navController.navigateToTaskEditor(dueToday = true) },
                onOpenTimetable = { openPlannerSection(PlannerSection.ROUTINE) },
                onOpenBlock = { blockId -> navController.navigateToRoutineBlock(blockId = blockId) },
                onOpenExam = { examId -> navController.navigateToExam(examId = examId) },
            )
        }
        composable<PlannerRoute> {
            PlannerScreen(
                onOpenTask = { taskId -> navController.navigateToTaskEditor(taskId) },
                onNewTask = { dueToday -> navController.navigateToTaskEditor(dueToday = dueToday) },
                onOpenBlock = { blockId -> navController.navigateToRoutineBlock(blockId = blockId) },
                onNewBlock = { date, startMinute -> navController.navigateToRoutineBlock(date = date, startMinute = startMinute) },
                onOpenSubject = { subjectId -> navController.navigateToSubject(subjectId) },
                onOpenExam = { examId -> navController.navigateToExam(examId = examId) },
                onNewExam = { navController.navigateToExam() },
                requestedSection = requestedPlannerSection,
                onSectionRequestHandled = { requestedPlannerSection = null },
            )
        }
        composable<FocusRoute> { FocusScreen() }
        composable<InsightsRoute> { InsightsScreen() }
        composable<AssistantRoute> { AssistantScreen() }
        // ===== END SECTION: NAV_HOST_TOP_LEVEL =====

        // ===== SECTION: NAV_HOST_SECONDARY =====
        composable<DistractionShieldRoute> { DistractionShieldScreen() }
        composable<SettingsRoute> { SettingsRoute() }
        composable<TaskEditorRoute> { entry ->
            val route = entry.toRoute<TaskEditorRoute>()
            TaskEditorScreenRoute(
                taskId = route.taskId,
                dueToday = route.dueToday,
                // Pop only while this editor is on top, so a late event can never pop twice.
                onClose = { if (navController.currentBackStackEntry?.id == entry.id) navController.popBackStack() },
            )
        }
        // ===== END SECTION: NAV_HOST_SECONDARY =====

        // ===== SECTION: NAV_HOST_PLANNER =====
        composable<RoutineBlockRoute> { entry ->
            val route = entry.toRoute<RoutineBlockRoute>()
            RoutineBlockEditorRoute(
                blockId = route.blockId,
                duplicateOf = route.duplicateOf,
                initialDate = route.epochDay.takeIf { it != RoutineBlockRoute.NO_EPOCH_DAY }?.let(LocalDate::ofEpochDay),
                initialStartMinute = route.startMinute.takeIf { it >= 0 },
                onClose = { if (navController.currentBackStackEntry?.id == entry.id) navController.popBackStack() },
                // The copy replaces this editor, so Back from it returns to the timetable.
                onOpenDuplicate = { blockId ->
                    if (navController.currentBackStackEntry?.id == entry.id) {
                        navController.navigateToRoutineBlock(duplicateOf = blockId, replaceCurrent = true)
                    }
                },
            )
        }
        composable<SubjectRoute> { entry ->
            val route = entry.toRoute<SubjectRoute>()
            SubjectDetailRoute(
                subjectId = route.subjectId,
                onClose = { if (navController.currentBackStackEntry?.id == entry.id) navController.popBackStack() },
                onOpenExam = { examId -> navController.navigateToExam(examId = examId) },
                onNewExam = { subjectId -> navController.navigateToExam(subjectId = subjectId) },
            )
        }
        composable<ExamRoute> { entry ->
            val route = entry.toRoute<ExamRoute>()
            ExamEditorRoute(
                examId = route.examId,
                presetSubjectId = route.subjectId,
                onClose = { if (navController.currentBackStackEntry?.id == entry.id) navController.popBackStack() },
            )
        }
        // ===== END SECTION: NAV_HOST_PLANNER =====
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

/** Opens the task editor; [taskId] 0 creates a task. */
fun NavHostController.navigateToTaskEditor(taskId: Long = 0L, dueToday: Boolean = false) {
    navigate(TaskEditorRoute(taskId = taskId, dueToday = dueToday)) { launchSingleTop = true }
}

/**
 * Opens the timetable block editor: [blockId] edits, [duplicateOf] copies, neither creates a
 * block (optionally at [date] and [startMinute]). [replaceCurrent] swaps out the editor on top.
 */
fun NavHostController.navigateToRoutineBlock(
    blockId: Long = 0L,
    duplicateOf: Long = 0L,
    date: LocalDate? = null,
    startMinute: Int? = null,
    replaceCurrent: Boolean = false,
) {
    val route = RoutineBlockRoute(
        blockId = blockId,
        duplicateOf = duplicateOf,
        epochDay = date?.toEpochDay() ?: RoutineBlockRoute.NO_EPOCH_DAY,
        startMinute = startMinute ?: -1,
    )
    navigate(route) {
        if (replaceCurrent) popUpTo<RoutineBlockRoute> { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSubject(subjectId: Long) {
    navigate(SubjectRoute(subjectId = subjectId)) { launchSingleTop = true }
}

/** Opens the exam editor; [examId] 0 creates one, optionally for [subjectId]. */
fun NavHostController.navigateToExam(examId: Long = 0L, subjectId: Long = 0L) {
    navigate(ExamRoute(examId = examId, subjectId = subjectId)) { launchSingleTop = true }
}
// ===== END SECTION: NAV_CONTROLLER_EXTENSIONS =====
