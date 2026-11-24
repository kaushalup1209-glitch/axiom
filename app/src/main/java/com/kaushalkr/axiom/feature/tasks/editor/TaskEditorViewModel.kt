package com.kaushalkr.axiom.feature.tasks.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.domain.task.TaskActions
import com.kaushalkr.axiom.core.domain.task.toDraft
import com.kaushalkr.axiom.core.model.SubtaskDraft
import com.kaushalkr.axiom.core.model.TaskDraft
import com.kaushalkr.axiom.core.model.TaskLimits
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.model.TaskRecurrence
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

// ===== SECTION: TASK_EDITOR_MODELS =====
/** A subtask row in the editor. [key] is stable for the list even before the row is saved. */
@Immutable
data class EditableSubtask(
    val key: Long,
    val id: Long,
    val title: String,
    val isDone: Boolean,
)

/** One-off results that close the editor. */
enum class TaskEditorEvent {
    SAVED,
    DELETED,
    NOT_FOUND,
}
// ===== END SECTION: TASK_EDITOR_MODELS =====

/**
 * Form state for creating ([taskId] = 0) or editing a task. Fields are Compose snapshot state,
 * so only the controls that read a field recompose when it changes. Saving and deleting go
 * through [TaskActions], the same path the AI assistant will use.
 */
class TaskEditorViewModel(
    private val taskId: Long,
    dueToday: Boolean,
    repository: TaskRepository,
    private val actions: TaskActions,
) : ViewModel() {

    // ===== SECTION: TASK_EDITOR_FIELDS =====
    val isNew: Boolean = taskId == 0L
    val today: LocalDate = repository.today()

    val titleState = TextFieldState()
    val descriptionState = TextFieldState()
    val subjectState = TextFieldState()
    val newSubtaskState = TextFieldState()

    var priority by mutableStateOf(TaskPriority.MEDIUM)
    var dueDate by mutableStateOf(if (isNew && dueToday) today else null)
        private set
    var dueTime by mutableStateOf<LocalTime?>(null)
    var estimatedMinutes by mutableStateOf<Int?>(null)
    var recurrence by mutableStateOf(TaskRecurrence.NONE)
        private set
    val subtasks = mutableStateListOf<EditableSubtask>()

    /** False until an existing task has loaded; the form is hidden meanwhile. */
    var isLoaded by mutableStateOf(isNew)
        private set
    var isCompleted by mutableStateOf(false)
        private set
    private var isSaving = false
    private var nextSubtaskKey = -1L
    private var original: TaskDraft = currentDraft()

    val subjects: StateFlow<List<String>> =
        repository.subjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val eventChannel = Channel<TaskEditorEvent>(Channel.BUFFERED)
    val events: Flow<TaskEditorEvent> = eventChannel.receiveAsFlow()
    // ===== END SECTION: TASK_EDITOR_FIELDS =====

    init {
        if (!isNew) {
            viewModelScope.launch {
                val task = repository.getTask(taskId)
                if (task == null) {
                    eventChannel.send(TaskEditorEvent.NOT_FOUND)
                    return@launch
                }
                val draft = task.toDraft()
                titleState.setTextAndPlaceCursorAtEnd(draft.title)
                descriptionState.setTextAndPlaceCursorAtEnd(draft.description)
                subjectState.setTextAndPlaceCursorAtEnd(draft.subject)
                priority = draft.priority
                dueDate = draft.parsedDueDate
                dueTime = draft.parsedDueTime
                estimatedMinutes = draft.estimatedMinutes
                recurrence = draft.recurrence
                subtasks.addAll(draft.subtasks.map { EditableSubtask(key = it.id, id = it.id, title = it.title, isDone = it.isDone) })
                isCompleted = task.isCompleted
                original = currentDraft()
                isLoaded = true
            }
        }
    }

    // ===== SECTION: TASK_EDITOR_SCHEDULE =====
    fun selectDueDate(date: LocalDate?) {
        dueDate = date
        if (date == null) {
            dueTime = null
            recurrence = TaskRecurrence.NONE
        }
    }

    /** Repeating needs a date to repeat from; picking a repeat without one starts today. */
    fun selectRecurrence(value: TaskRecurrence) {
        recurrence = value
        if (value != TaskRecurrence.NONE && dueDate == null) dueDate = today
    }
    // ===== END SECTION: TASK_EDITOR_SCHEDULE =====

    // ===== SECTION: TASK_EDITOR_SUBTASKS =====
    val canAddSubtask: Boolean get() = subtasks.size < TaskLimits.MAX_SUBTASKS

    fun addSubtask() {
        val title = newSubtaskState.text.toString().trim()
        if (title.isEmpty() || !canAddSubtask) return
        subtasks.add(EditableSubtask(key = nextSubtaskKey--, id = 0L, title = title, isDone = false))
        newSubtaskState.clearText()
    }

    fun toggleSubtask(key: Long) {
        val index = subtasks.indexOfFirst { it.key == key }
        if (index >= 0) subtasks[index] = subtasks[index].let { it.copy(isDone = !it.isDone) }
    }

    fun removeSubtask(key: Long) {
        subtasks.removeAll { it.key == key }
    }
    // ===== END SECTION: TASK_EDITOR_SUBTASKS =====

    // ===== SECTION: TASK_EDITOR_SAVE =====
    val canSave: Boolean get() = isLoaded && titleState.text.isNotBlank()

    /** True when the form differs from what was loaded (or from a blank form). */
    val hasChanges: Boolean get() = isLoaded && currentDraft() != original

    fun save() {
        if (!canSave || isSaving) return
        isSaving = true
        val draft = currentDraft()
        viewModelScope.launch {
            actions.save(taskId, draft)
            eventChannel.send(TaskEditorEvent.SAVED)
        }
    }

    /** Deletes the task with undo offered on the next screen. */
    fun delete() {
        if (isNew || isSaving) return
        isSaving = true
        actions.delete(taskId, original.title)
        viewModelScope.launch { eventChannel.send(TaskEditorEvent.DELETED) }
    }

    private fun currentDraft(): TaskDraft = TaskDraft(
        title = titleState.text.toString().trim(),
        description = descriptionState.text.toString().trim(),
        subject = subjectState.text.toString().trim(),
        priority = priority,
        dueDate = dueDate?.toString(),
        dueTime = dueTime?.takeIf { dueDate != null }?.let { String.format(Locale.ROOT, "%02d:%02d", it.hour, it.minute) },
        estimatedMinutes = estimatedMinutes,
        recurrence = recurrence,
        subtasks = subtasks.map { SubtaskDraft(title = it.title, isDone = it.isDone, id = it.id) },
    )
    // ===== END SECTION: TASK_EDITOR_SAVE =====

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(taskId: Long, dueToday: Boolean): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                TaskEditorViewModel(taskId, dueToday, container.taskRepository, container.taskActions)
            }
        }
    }
}
