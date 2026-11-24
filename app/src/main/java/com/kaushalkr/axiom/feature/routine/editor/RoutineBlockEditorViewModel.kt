package com.kaushalkr.axiom.feature.routine.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.academic.SubjectRepository
import com.kaushalkr.axiom.core.data.routine.RoutineRepository
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.domain.routine.ConflictResolutions
import com.kaushalkr.axiom.core.domain.routine.RoutineActions
import com.kaushalkr.axiom.core.domain.routine.RoutineConflict
import com.kaushalkr.axiom.core.domain.routine.RoutineConflictDetector
import com.kaushalkr.axiom.core.domain.routine.formatMinute
import com.kaushalkr.axiom.core.domain.routine.toCandidate
import com.kaushalkr.axiom.core.model.LinkedTask
import com.kaushalkr.axiom.core.model.MINUTES_PER_DAY
import com.kaushalkr.axiom.core.model.RoutineAutomation
import com.kaushalkr.axiom.core.model.RoutineBlock
import com.kaushalkr.axiom.core.model.RoutineBlockDraft
import com.kaushalkr.axiom.core.model.RoutineKind
import com.kaushalkr.axiom.core.model.RoutineLimits
import com.kaushalkr.axiom.core.model.SubjectRef
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.core.model.Weekdays
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

// ===== SECTION: ROUTINE_EDITOR_MODELS =====
/** One-off results of the editor. */
sealed interface RoutineEditorEvent {
    data object Closed : RoutineEditorEvent

    /** The form overlaps other blocks; show the resolution sheet. */
    data object ShowConflicts : RoutineEditorEvent

    data class OpenDuplicate(val blockId: Long) : RoutineEditorEvent
}

/** How the block repeats. */
enum class RepeatMode {
    WEEKLY,
    ONE_DAY,
}
// ===== END SECTION: ROUTINE_EDITOR_MODELS =====

/**
 * Form state for creating, editing or duplicating a timetable block. Conflicts with the rest of
 * the timetable are recomputed off the main thread (debounced) as the time, length or days change.
 *
 * @param duplicateOf copy this block into a new, unsaved one that starts where it ends.
 * @param initialDate the day the user started from (defaults the repeat day and one-off date).
 * @param initialStartMinute a tapped timeline time, or null for the next half hour.
 */
@OptIn(FlowPreview::class)
class RoutineBlockEditorViewModel(
    private val blockId: Long,
    private val duplicateOf: Long,
    initialDate: LocalDate?,
    initialStartMinute: Int?,
    private val routines: RoutineRepository,
    subjectRepository: SubjectRepository,
    taskRepository: TaskRepository,
    private val actions: RoutineActions,
    timeProvider: TimeProvider,
) : ViewModel() {

    // ===== SECTION: ROUTINE_EDITOR_FIELDS =====
    val isNew: Boolean = blockId == 0L
    val today: LocalDate = routines.today()
    private val startDate: LocalDate = initialDate ?: today

    val titleState = TextFieldState()
    val notesState = TextFieldState()

    var kind by mutableStateOf(RoutineKind.STUDY)
    var subjectId by mutableStateOf<Long?>(null)
    var startMinute by mutableStateOf(initialStartMinute ?: nextHalfHour(timeProvider))
        private set
    var durationMinutes by mutableStateOf(DEFAULT_DURATION_MINUTES)
        private set
    var repeatMode by mutableStateOf(RepeatMode.WEEKLY)
        private set
    var daysMask by mutableStateOf(Weekdays.bit(startDate.dayOfWeek))
        private set
    var oneOffDate by mutableStateOf(startDate)
        private set
    var automation by mutableStateOf(RoutineAutomation())
    val linkedTasks = mutableStateListOf<LinkedTask>()

    var isLoaded by mutableStateOf(isNew && duplicateOf == 0L)
        private set
    var resolutions by mutableStateOf<ConflictResolutions?>(null)
        private set
    private var isSaving = false
    private var original: RoutineBlockDraft = currentDraft(fallbackTitle = "")

    val subjects: StateFlow<List<SubjectRef>> =
        subjectRepository.subjectRefs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val openTasks: StateFlow<List<Task>> =
        taskRepository.openTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    /** Blocks this form overlaps, kept current while the user edits. */
    val conflicts: StateFlow<List<RoutineConflict>> = combine(
        snapshotFlow { candidate() },
        routines.blocks,
    ) { candidate, blocks -> RoutineConflictDetector.conflicts(candidate, blocks, today) }
        .debounce(CONFLICT_DEBOUNCE_MILLIS)
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val eventChannel = Channel<RoutineEditorEvent>(Channel.BUFFERED)
    val events: Flow<RoutineEditorEvent> = eventChannel.receiveAsFlow()
    // ===== END SECTION: ROUTINE_EDITOR_FIELDS =====

    init {
        val sourceId = if (!isNew) blockId else duplicateOf
        if (sourceId != 0L) {
            viewModelScope.launch {
                val block = routines.getBlock(sourceId)
                if (block == null) {
                    eventChannel.send(RoutineEditorEvent.Closed)
                    return@launch
                }
                load(block, asDuplicate = isNew)
                original = currentDraft(fallbackTitle = "")
                isLoaded = true
            }
        }
    }

    private fun load(block: RoutineBlock, asDuplicate: Boolean) {
        titleState.setTextAndPlaceCursorAtEnd(block.title)
        notesState.setTextAndPlaceCursorAtEnd(block.notes)
        kind = block.kind
        subjectId = block.subject?.id
        durationMinutes = block.durationMinutes
        // A duplicate starts where the original ends (same day), so it does not overlap it.
        startMinute = if (asDuplicate && block.endMinute < MINUTES_PER_DAY) block.endMinute else block.startMinute
        if (block.isRecurring) {
            repeatMode = RepeatMode.WEEKLY
            daysMask = block.daysMask
        } else {
            repeatMode = RepeatMode.ONE_DAY
            oneOffDate = block.date ?: today
        }
        automation = block.automation
        linkedTasks.clear()
        linkedTasks.addAll(block.linkedTasks)
    }

    // ===== SECTION: ROUTINE_EDITOR_TIME =====
    fun selectStart(minute: Int) {
        startMinute = minute.coerceIn(0, MINUTES_PER_DAY - 1)
    }

    fun selectDuration(minutes: Int) {
        durationMinutes = minutes.coerceIn(RoutineLimits.MIN_DURATION_MINUTES, RoutineLimits.MAX_DURATION_MINUTES)
    }

    /** Sets the length from an end time; an end at or before the start means the next day. */
    fun selectEnd(endMinuteOfDay: Int) {
        var duration = endMinuteOfDay - startMinute
        if (duration <= 0) duration += MINUTES_PER_DAY
        selectDuration(duration)
    }
    // ===== END SECTION: ROUTINE_EDITOR_TIME =====

    // ===== SECTION: ROUTINE_EDITOR_REPEAT =====
    fun setDays(mask: Int) {
        repeatMode = RepeatMode.WEEKLY
        daysMask = if (mask == Weekdays.NONE) Weekdays.bit(startDate.dayOfWeek) else mask
    }

    fun toggleDay(day: DayOfWeek) {
        val next = Weekdays.toggle(daysMask, day)
        // Keep at least one day; switching to a single date has its own option.
        if (next != Weekdays.NONE) daysMask = next
        repeatMode = RepeatMode.WEEKLY
    }

    fun selectOneDay(date: LocalDate) {
        repeatMode = RepeatMode.ONE_DAY
        oneOffDate = date
    }

    fun useOneDay() {
        repeatMode = RepeatMode.ONE_DAY
    }
    // ===== END SECTION: ROUTINE_EDITOR_REPEAT =====

    // ===== SECTION: ROUTINE_EDITOR_TASKS =====
    fun toggleTask(task: Task) {
        val index = linkedTasks.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            linkedTasks.removeAt(index)
        } else if (linkedTasks.size < RoutineLimits.MAX_LINKED_TASKS) {
            linkedTasks.add(LinkedTask(id = task.id, title = task.title, isCompleted = task.isCompleted))
        }
    }

    fun unlinkTask(taskId: Long) {
        linkedTasks.removeAll { it.id == taskId }
    }
    // ===== END SECTION: ROUTINE_EDITOR_TASKS =====

    // ===== SECTION: ROUTINE_EDITOR_SAVE =====
    val hasChanges: Boolean get() = isLoaded && currentDraft(fallbackTitle = "") != original

    /**
     * Saves, unless the block overlaps others and [force] is false; then the resolution sheet is
     * requested instead. [fallbackTitle] names a block saved without a title.
     */
    fun save(fallbackTitle: String, force: Boolean = false) {
        if (!isLoaded || isSaving) return
        viewModelScope.launch {
            val draft = currentDraft(fallbackTitle)
            if (!force) {
                val candidate = draft.toCandidate(blockId, today)
                val found = withContext(Dispatchers.Default) {
                    RoutineConflictDetector.conflicts(candidate, routines.blocks.first(), today)
                }
                if (found.isNotEmpty()) {
                    resolutions = withContext(Dispatchers.Default) {
                        RoutineConflictDetector.resolutions(candidate, routines.blocks.first(), today)
                    }
                    eventChannel.send(RoutineEditorEvent.ShowConflicts)
                    return@launch
                }
            }
            isSaving = true
            actions.save(blockId, draft)
            eventChannel.send(RoutineEditorEvent.Closed)
        }
    }

    /** Applies a proposed fix from the conflict sheet and saves. */
    fun resolveByMoving(fallbackTitle: String) {
        val start = resolutions?.nextFreeStartMinute ?: return
        selectStart(start)
        save(fallbackTitle, force = true)
    }

    fun resolveByShortening(fallbackTitle: String) {
        val duration = resolutions?.shortenedDurationMinutes ?: return
        selectDuration(duration)
        save(fallbackTitle, force = true)
    }

    fun delete() {
        if (isNew || isSaving) return
        isSaving = true
        actions.delete(blockId, original.title)
        viewModelScope.launch { eventChannel.send(RoutineEditorEvent.Closed) }
    }

    fun duplicate() {
        if (isNew) return
        viewModelScope.launch { eventChannel.send(RoutineEditorEvent.OpenDuplicate(blockId)) }
    }

    private fun candidate(): RoutineBlock = currentDraft(fallbackTitle = "").toCandidate(blockId, today)

    private fun currentDraft(fallbackTitle: String): RoutineBlockDraft {
        val title = titleState.text.toString().trim().ifEmpty { fallbackTitle }
        val isWeekly = repeatMode == RepeatMode.WEEKLY
        return RoutineBlockDraft(
            title = title,
            kind = kind,
            subjectId = subjectId.takeIf { kind == RoutineKind.STUDY },
            startTime = formatMinute(startMinute),
            durationMinutes = durationMinutes,
            days = if (isWeekly) Weekdays.toIsoNumbers(daysMask) else emptyList(),
            date = if (isWeekly) null else oneOffDate.toString(),
            notes = notesState.text.toString().trim(),
            linkedTaskIds = linkedTasks.map { it.id },
            automation = automation,
        )
    }
    // ===== END SECTION: ROUTINE_EDITOR_SAVE =====

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val CONFLICT_DEBOUNCE_MILLIS = 150L
        private const val DEFAULT_DURATION_MINUTES = 60
        private const val HALF_HOUR = 30

        private fun nextHalfHour(timeProvider: TimeProvider): Int {
            val now = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeProvider.nowMillis()), timeProvider.zone())
            val minute = now.hour * 60 + now.minute
            return ((minute / HALF_HOUR + 1) * HALF_HOUR).coerceAtMost(MINUTES_PER_DAY - HALF_HOUR)
        }

        fun factory(blockId: Long, duplicateOf: Long, initialDate: LocalDate?, initialStartMinute: Int?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                    RoutineBlockEditorViewModel(
                        blockId = blockId,
                        duplicateOf = duplicateOf,
                        initialDate = initialDate,
                        initialStartMinute = initialStartMinute,
                        routines = container.routineRepository,
                        subjectRepository = container.subjectRepository,
                        taskRepository = container.taskRepository,
                        actions = container.routineActions,
                        timeProvider = container.timeProvider,
                    )
                }
            }
    }
}
