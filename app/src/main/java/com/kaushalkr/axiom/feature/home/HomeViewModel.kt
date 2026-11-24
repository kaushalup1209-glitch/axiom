package com.kaushalkr.axiom.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.focus.FocusSessionRepository
import com.kaushalkr.axiom.core.data.sample.PlannerPreview
import com.kaushalkr.axiom.core.data.sample.PlannerPreviewData
import com.kaushalkr.axiom.core.data.study.StudyPreferencesRepository
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.domain.StudyStreakCalculator
import com.kaushalkr.axiom.core.domain.task.TaskActions
import com.kaushalkr.axiom.core.model.ActiveFocusSession
import com.kaushalkr.axiom.core.model.StudyDefaults
import com.kaushalkr.axiom.core.platform.TimeProvider
import com.kaushalkr.axiom.feature.tasks.TaskListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Home dashboard state. Ticks run only while the screen is visible (WhileSubscribed):
 * the dashboard updates on the minute, the focus clock on the second, and only while a
 * session is running.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val studyPreferences: StudyPreferencesRepository,
    private val focusSessions: FocusSessionRepository,
    private val tasks: TaskRepository,
    private val taskActions: TaskActions,
    private val timeProvider: TimeProvider,
    private val plannerPreview: PlannerPreview = PlannerPreviewData.Default,
) : ViewModel() {

    // ===== SECTION: HOME_VM_TIME =====
    private val minuteTicks = timeProvider.ticks(MINUTE_MILLIS)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), replay = 1)

    private val todayFlow = minuteTicks
        .map { now -> localDateTime(now).toLocalDate() }
        .distinctUntilChanged()

    private val dailyTotals = todayFlow.flatMapLatest { today ->
        focusSessions.dailyFocusTotals(sinceDate = today.minusDays(STREAK_LOOKBACK_DAYS))
    }

    private val taskSummary = todayFlow.flatMapLatest { today ->
        combine(tasks.openTasks, tasks.completedSince(today)) { open, completedToday ->
            HomeTaskSummary.from(open, completedToday, today)
        }
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
    // ===== END SECTION: HOME_VM_TIME =====

    // ===== SECTION: HOME_VM_DASHBOARD =====
    val dashboard: StateFlow<HomeDashboardUiState?> = combine(
        minuteTicks,
        studyPreferences.preferences,
        dailyTotals,
        focusSessions.activeSession,
        taskSummary,
    ) { now, preferences, totals, active, taskSummary ->
        val dateTime = localDateTime(now)
        val today = dateTime.toLocalDate()
        val liveTodayMillis = (totals[today] ?: 0L) + (active?.focusedMillis(now) ?: 0L)
        val totalsWithLive = totals + (today to liveTodayMillis)
        HomeDashboardUiState(
            today = today,
            currentTime = dateTime.toLocalTime().withSecond(0).withNano(0),
            dayPart = DayPart.from(dateTime.toLocalTime()),
            userName = preferences.userName,
            studiedTodayMinutes = (liveTodayMillis / MINUTE_MILLIS).toInt(),
            dailyGoalMinutes = preferences.dailyGoalMinutes,
            streak = StudyStreakCalculator.calculate(
                dailyFocusMillis = totalsWithLive,
                today = today,
                minimumMillis = StudyDefaults.STREAK_MINIMUM_MINUTES * MINUTE_MILLIS,
            ),
            distractionTodayMinutes = null,
            tasks = taskSummary,
            plannerPreview = plannerPreview,
        )
    }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initialValue = null)
    // ===== END SECTION: HOME_VM_DASHBOARD =====

    // ===== SECTION: HOME_VM_QUICK_FOCUS =====
    val quickFocusMode: StateFlow<QuickFocusMode> = combine(
        focusSessions.activeSession,
        studyPreferences.preferences.map { it.quickStartMinutes }.distinctUntilChanged(),
    ) { active, quickStartMinutes -> active.toMode(quickStartMinutes) }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = QuickFocusMode.Idle(StudyDefaults.QUICK_START_MINUTES, StudyDefaults.QUICK_START_PRESETS_MINUTES),
        )

    /** Ticks every second only while a session is running; otherwise emits once. */
    val quickFocusClock: StateFlow<QuickFocusClock> = combine(
        focusSessions.activeSession,
        studyPreferences.preferences.map { it.quickStartMinutes }.distinctUntilChanged(),
    ) { active, quickStartMinutes -> active to quickStartMinutes }
        .flatMapLatest { (active, quickStartMinutes) ->
            when {
                active == null -> flowOf(QuickFocusClock(quickStartMinutes * MINUTE_MILLIS, 0f))
                active.isPaused -> flowOf(active.clockAt(timeProvider.nowMillis()))
                else -> timeProvider.ticks(SECOND_MILLIS)
                    .map { now -> active.clockAt(now) }
                    .onEach { clock -> if (clock.remainingMillis <= 0L) focusSessions.completeIfDue() }
            }
        }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = QuickFocusClock(StudyDefaults.QUICK_START_MINUTES * MINUTE_MILLIS, 0f),
        )

    init {
        // A session may have reached its planned end while the app was closed.
        viewModelScope.launch { focusSessions.completeIfDue() }
    }

    fun onPresetSelected(minutes: Int) {
        viewModelScope.launch { studyPreferences.setQuickStartMinutes(minutes) }
    }

    fun onStartFocus() {
        val mode = quickFocusMode.value as? QuickFocusMode.Idle ?: return
        viewModelScope.launch { focusSessions.start(mode.selectedMinutes) }
    }

    fun onPauseFocus() {
        viewModelScope.launch { focusSessions.pause() }
    }

    fun onResumeFocus() {
        viewModelScope.launch { focusSessions.resume() }
    }

    fun onFinishFocus() {
        viewModelScope.launch { focusSessions.finish() }
    }
    // ===== END SECTION: HOME_VM_QUICK_FOCUS =====

    // ===== SECTION: HOME_VM_TASKS =====
    /** Completes (with undo) or reopens a task from the dashboard. */
    fun onToggleTask(item: TaskListItem) {
        if (item.isCompleted) taskActions.reopen(item.id) else taskActions.complete(item.id, item.title)
    }
    // ===== END SECTION: HOME_VM_TASKS =====

    // ===== SECTION: HOME_VM_HELPERS =====
    private fun localDateTime(epochMillis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), timeProvider.zone())

    private fun ActiveFocusSession?.toMode(quickStartMinutes: Int): QuickFocusMode {
        val session = this ?: return QuickFocusMode.Idle(quickStartMinutes, StudyDefaults.QUICK_START_PRESETS_MINUTES)
        val plannedMinutes = (session.plannedDurationMillis / MINUTE_MILLIS).toInt()
        return if (session.isPaused) QuickFocusMode.Paused(plannedMinutes) else QuickFocusMode.Running(plannedMinutes)
    }

    private fun ActiveFocusSession.clockAt(nowMillis: Long): QuickFocusClock =
        QuickFocusClock(
            remainingMillis = remainingMillis(nowMillis).coerceAtLeast(0L),
            progress = progress(nowMillis),
        )

    companion object {
        private const val SECOND_MILLIS = 1_000L
        private const val MINUTE_MILLIS = 60_000L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val STREAK_LOOKBACK_DAYS = 400L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                HomeViewModel(
                    studyPreferences = container.studyPreferencesRepository,
                    focusSessions = container.focusSessionRepository,
                    tasks = container.taskRepository,
                    taskActions = container.taskActions,
                    timeProvider = container.timeProvider,
                )
            }
        }
    }
    // ===== END SECTION: HOME_VM_HELPERS =====
}
