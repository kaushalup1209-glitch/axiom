package com.kaushalkr.axiom.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.designsystem.components.AxiomScreenColumn
import com.kaushalkr.axiom.core.designsystem.components.ScreenHeader
import com.kaushalkr.axiom.core.designsystem.layout.LocalAxiomWidthClass
import com.kaushalkr.axiom.core.designsystem.motion.AxiomMotionTokens
import com.kaushalkr.axiom.core.designsystem.theme.AxiomSpacing
import com.kaushalkr.axiom.core.designsystem.theme.AxiomTheme
import com.kaushalkr.axiom.feature.home.components.DailyGoalCard
import com.kaushalkr.axiom.feature.home.components.QuickFocusCard
import com.kaushalkr.axiom.feature.home.components.StreakCard
import com.kaushalkr.axiom.feature.home.components.StudyStatTile
import com.kaushalkr.axiom.feature.home.components.TodayRoutineCard
import com.kaushalkr.axiom.feature.home.components.TodayTasksCard
import com.kaushalkr.axiom.feature.tasks.TaskListItem

// ===== SECTION: HOME_ROUTE =====
/** Stateful entry point: connects [HomeViewModel] to the stateless [HomeScreen]. */
@Composable
fun HomeDashboardRoute(
    onOpenDistractionShield: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenTask: (taskId: Long) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val focusMode by viewModel.quickFocusMode.collectAsStateWithLifecycle()
    // Passed down as State so only the timer readout reads (and recomposes on) each tick.
    val focusClock = viewModel.quickFocusClock.collectAsStateWithLifecycle()
    val motion = AxiomTheme.motion
    // Data arrives a few frames after the screen; fade it in instead of popping it in.
    AnimatedVisibility(
        visible = dashboard != null,
        enter = fadeIn(motion.tween(AxiomMotionTokens.ContentSwapIn)),
        exit = fadeOut(motion.tween(AxiomMotionTokens.ContentSwapOut)),
    ) {
        val loadedDashboard = dashboard ?: return@AnimatedVisibility
        HomeScreen(
        dashboard = loadedDashboard,
        focusMode = focusMode,
        focusClock = focusClock,
        onPresetSelected = viewModel::onPresetSelected,
        onStartFocus = viewModel::onStartFocus,
        onPauseFocus = viewModel::onPauseFocus,
        onResumeFocus = viewModel::onResumeFocus,
        onFinishFocus = viewModel::onFinishFocus,
        onOpenDistractionShield = onOpenDistractionShield,
        onToggleTask = viewModel::onToggleTask,
        onOpenTask = onOpenTask,
        onAddTask = onAddTask,
        onOpenPlanner = onOpenPlanner,
        modifier = modifier,
    )
    }
}
// ===== END SECTION: HOME_ROUTE =====

// ===== SECTION: HOME_SCREEN =====
/**
 * Home dashboard. Phones show one column; medium and expanded widths split into two
 * columns (today's progress on the left, plan on the right).
 */
@Composable
fun HomeScreen(
    dashboard: HomeDashboardUiState,
    focusMode: QuickFocusMode,
    focusClock: State<QuickFocusClock>,
    onPresetSelected: (Int) -> Unit,
    onStartFocus: () -> Unit,
    onPauseFocus: () -> Unit,
    onResumeFocus: () -> Unit,
    onFinishFocus: () -> Unit,
    onOpenDistractionShield: () -> Unit,
    onToggleTask: (TaskListItem) -> Unit,
    onOpenTask: (Long) -> Unit,
    onAddTask: () -> Unit,
    onOpenPlanner: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWide = LocalAxiomWidthClass.current.isWide
    val progressColumn: @Composable (Modifier) -> Unit = { columnModifier ->
        Column(modifier = columnModifier, verticalArrangement = Arrangement.spacedBy(AxiomSpacing.lg)) {
            DailyGoalCard(
                studiedMinutes = dashboard.studiedTodayMinutes,
                goalMinutes = dashboard.dailyGoalMinutes,
                progress = dashboard.goalProgress,
                isGoalReached = dashboard.isGoalReached,
                modifier = Modifier.fillMaxWidth(),
            )
            QuickFocusCard(
                mode = focusMode,
                clock = focusClock,
                onPresetSelected = onPresetSelected,
                onStart = onStartFocus,
                onPause = onPauseFocus,
                onResume = onResumeFocus,
                onFinish = onFinishFocus,
                modifier = Modifier.fillMaxWidth(),
            )
            HomeStatsRow(dashboard, onOpenDistractionShield)
        }
    }
    val planColumn: @Composable (Modifier) -> Unit = { columnModifier ->
        Column(modifier = columnModifier, verticalArrangement = Arrangement.spacedBy(AxiomSpacing.lg)) {
            TodayTasksCard(
                summary = dashboard.tasks,
                today = dashboard.today,
                onToggleTask = onToggleTask,
                onOpenTask = onOpenTask,
                onAddTask = onAddTask,
                onOpenPlanner = onOpenPlanner,
                modifier = Modifier.fillMaxWidth(),
            )
            StreakCard(streak = dashboard.streak, modifier = Modifier.fillMaxWidth())
            TodayRoutineCard(
                routine = dashboard.plannerPreview.routine,
                currentTime = dashboard.currentTime,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    AxiomScreenColumn(modifier = modifier) {
        HomeHeader(dashboard)
        if (isWide) {
            Row(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.lg)) {
                progressColumn(Modifier.weight(1f))
                planColumn(Modifier.weight(1f))
            }
        } else {
            progressColumn(Modifier.fillMaxWidth())
            planColumn(Modifier.fillMaxWidth())
        }
    }
}
// ===== END SECTION: HOME_SCREEN =====

// ===== SECTION: HOME_HEADER =====
@Composable
private fun HomeHeader(dashboard: HomeDashboardUiState) {
    val greeting = stringResource(
        when (dashboard.dayPart) {
            DayPart.MORNING -> R.string.home_greeting_morning
            DayPart.AFTERNOON -> R.string.home_greeting_afternoon
            DayPart.EVENING -> R.string.home_greeting_evening
            DayPart.LATE_NIGHT -> R.string.home_greeting_late_night
        },
    )
    val title = if (dashboard.userName.isBlank()) {
        greeting
    } else {
        stringResource(R.string.home_greeting_with_name, greeting, dashboard.userName)
    }
    val summary = when {
        dashboard.isGoalReached -> stringResource(R.string.home_summary_goal_reached)
        dashboard.studiedTodayMinutes > 0 ->
            stringResource(R.string.home_summary_progress, formatStudyMinutes(dashboard.studiedTodayMinutes))
        else -> stringResource(R.string.home_summary_none)
    }
    ScreenHeader(
        eyebrow = rememberFormattedDate(dashboard.today),
        title = title,
        subtitle = summary,
        modifier = Modifier.padding(bottom = AxiomSpacing.xs),
    )
}
// ===== END SECTION: HOME_HEADER =====

// ===== SECTION: HOME_STATS_ROW =====
@Composable
private fun HomeStatsRow(
    dashboard: HomeDashboardUiState,
    onOpenDistractionShield: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AxiomSpacing.md)) {
        StudyStatTile(
            iconRes = R.drawable.ic_study_time,
            label = stringResource(R.string.home_stat_study_time),
            value = formatStudyMinutes(dashboard.studiedTodayMinutes),
            caption = stringResource(R.string.home_stat_study_caption),
            modifier = Modifier.weight(1f),
        )
        val distraction = dashboard.distractionTodayMinutes
        StudyStatTile(
            iconRes = R.drawable.ic_shield,
            label = stringResource(R.string.home_stat_distraction),
            value = if (distraction == null) stringResource(R.string.home_stat_distraction_untracked) else formatStudyMinutes(distraction),
            caption = stringResource(R.string.home_stat_distraction_caption),
            isValueMuted = distraction == null,
            onClick = onOpenDistractionShield,
            modifier = Modifier.weight(1f),
        )
    }
}
// ===== END SECTION: HOME_STATS_ROW =====
