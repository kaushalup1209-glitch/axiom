package com.kaushalkr.axiom.feature.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.academic.ExamRepository
import com.kaushalkr.axiom.core.data.routine.RoutineRepository
import com.kaushalkr.axiom.core.domain.routine.RoutineActions
import com.kaushalkr.axiom.core.domain.routine.RoutineConflictDetector
import com.kaushalkr.axiom.core.domain.routine.RoutineSchedule
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineBlock
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Timetable state for the day, week and month views. Everything is derived from the full block
 * list on [Dispatchers.Default]; the clock ticks once a minute and only while visible.
 */
class RoutineViewModel(
    private val routines: RoutineRepository,
    exams: ExamRepository,
    private val actions: RoutineActions,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    // ===== SECTION: ROUTINE_VM_INPUTS =====
    private val view = MutableStateFlow(RoutineView.DAY)

    /** Null follows today, so the timetable rolls over at midnight on its own. */
    private val pickedDate = MutableStateFlow<LocalDate?>(null)

    private val now = timeProvider.ticks(MINUTE_MILLIS)
        .map { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), timeProvider.zone()) }
        .map { it.withSecond(0).withNano(0) }
        .distinctUntilChanged()

    private val controls = combine(view, pickedDate) { view, date -> view to date }
    // ===== END SECTION: ROUTINE_VM_INPUTS =====

    // ===== SECTION: ROUTINE_VM_STATE =====
    val uiState: StateFlow<RoutineUiState?> = combine(routines.blocks, exams.exams, now, controls) { blocks, exams, now, controls ->
        build(blocks, exams, now, controls.first, controls.second ?: now.toLocalDate())
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initialValue = null)

    private fun build(blocks: List<RoutineBlock>, exams: List<Exam>, now: LocalDateTime, view: RoutineView, selected: LocalDate): RoutineUiState {
        val today = now.toLocalDate()
        val examsByDate = exams.groupBy { it.date }
        val dayOccurrences = RoutineSchedule.occurrencesOn(blocks, selected)
        val weekStart = selected.with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek))
        val month = YearMonth.from(selected)
        return RoutineUiState(
            view = view,
            selectedDate = selected,
            today = today,
            nowMinute = now.hour * 60 + now.minute,
            day = RoutineDayUi(
                date = selected,
                occurrences = RoutineSchedule.layoutLanes(dayOccurrences, minSpanMinutes = MIN_VISUAL_MINUTES),
                plannedMinutes = dayOccurrences.sumOf { it.durationMinutes },
                exams = examsByDate[selected].orEmpty(),
            ),
            week = (0L until DAYS_PER_WEEK).map { offset ->
                val date = weekStart.plusDays(offset)
                val occurrences = RoutineSchedule.occurrencesOn(blocks, date)
                RoutineWeekDayUi(date, occurrences, occurrences.sumOf { it.durationMinutes }, examsByDate[date].orEmpty())
            },
            month = RoutineMonthUi(
                month = month,
                marks = (1..month.lengthOfMonth()).associate { day ->
                    val date = month.atDay(day)
                    val occurrences = RoutineSchedule.occurrencesOn(blocks, date).filter { !it.isContinuation }
                    date to RoutineDayMark(
                        kinds = occurrences.map { it.block.kind }.distinct(),
                        blockCount = occurrences.size,
                        examCount = examsByDate[date]?.size ?: 0,
                    )
                },
                exams = exams.filter { YearMonth.from(it.date) == month },
            ),
            hasAnyBlocks = blocks.isNotEmpty(),
        )
    }
    // ===== END SECTION: ROUTINE_VM_STATE =====

    // ===== SECTION: ROUTINE_VM_ACTIONS =====
    fun selectView(newView: RoutineView) {
        view.value = newView
    }

    fun selectDate(date: LocalDate, showDay: Boolean = false) {
        pickedDate.value = date.takeIf { it != routines.today() }
        if (showDay) view.value = RoutineView.DAY
    }

    /** Steps one day, week or month back ([direction] = -1) or forward (+1). */
    fun step(direction: Int) {
        val current = pickedDate.value ?: routines.today()
        val next = when (view.value) {
            RoutineView.DAY -> current.plusDays(direction.toLong())
            RoutineView.WEEK -> current.plusWeeks(direction.toLong())
            RoutineView.MONTH -> current.plusMonths(direction.toLong())
        }
        selectDate(next)
    }

    fun goToToday() {
        pickedDate.value = null
    }

    /**
     * Moves a block by [deltaMinutes] (every occurrence). The undo message says when the new
     * time overlaps another block, so the user can put it back.
     */
    fun moveBlock(blockId: Long, deltaMinutes: Int) {
        if (deltaMinutes == 0) return
        viewModelScope.launch(Dispatchers.Default) {
            val all = routines.blocks.first()
            val block = all.firstOrNull { it.id == blockId } ?: return@launch
            val start = (block.startMinute + deltaMinutes).coerceIn(0, MINUTES_PER_DAY - 1)
            if (start == block.startMinute) return@launch
            val overlaps = RoutineConflictDetector.conflicts(block.copy(startMinute = start), all, routines.today()).isNotEmpty()
            actions.move(block.id, block.title, start, overlaps)
        }
    }

    fun deleteBlock(blockId: Long, title: String) {
        actions.delete(blockId, title)
    }
    // ===== END SECTION: ROUTINE_VM_ACTIONS =====

    companion object {
        private const val MINUTE_MILLIS = 60_000L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val DAYS_PER_WEEK = 7L

        /** Blocks shorter than this are drawn at this height, so lanes treat them as this long. */
        const val MIN_VISUAL_MINUTES = 30

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                RoutineViewModel(container.routineRepository, container.examRepository, container.routineActions, container.timeProvider)
            }
        }
    }
}
