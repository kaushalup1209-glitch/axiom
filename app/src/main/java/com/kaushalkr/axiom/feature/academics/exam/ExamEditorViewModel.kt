package com.kaushalkr.axiom.feature.academics.exam

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.core.data.academic.ExamRepository
import com.kaushalkr.axiom.core.data.academic.SubjectRepository
import com.kaushalkr.axiom.core.domain.academic.AcademicActions
import com.kaushalkr.axiom.core.domain.routine.formatMinute
import com.kaushalkr.axiom.core.model.ExamDraft
import com.kaushalkr.axiom.core.model.ExamKind
import com.kaushalkr.axiom.core.model.SubjectRef
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Form state for creating ([examId] = 0) or editing an exam or deadline.
 *
 * @param presetSubjectId pre-selects a subject when opened from a subject's page (0 for none).
 */
class ExamEditorViewModel(
    private val examId: Long,
    presetSubjectId: Long,
    private val exams: ExamRepository,
    subjectRepository: SubjectRepository,
    private val actions: AcademicActions,
    timeProvider: TimeProvider,
) : ViewModel() {

    // ===== SECTION: EXAM_EDITOR_FIELDS =====
    val isNew: Boolean = examId == 0L
    val today: LocalDate = Instant.ofEpochMilli(timeProvider.nowMillis()).atZone(timeProvider.zone()).toLocalDate()

    val titleState = TextFieldState()
    val notesState = TextFieldState()
    var kind by mutableStateOf(ExamKind.EXAM)
    var subjectId by mutableStateOf(presetSubjectId.takeIf { it != 0L })
    var date by mutableStateOf(today.plusWeeks(1))
    var time by mutableStateOf<LocalTime?>(null)
    var isLoaded by mutableStateOf(isNew)
        private set
    private var isSaving = false
    private var original: ExamDraft = currentDraft()

    val subjects: StateFlow<List<SubjectRef>> =
        subjectRepository.subjectRefs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val closeChannel = Channel<Unit>(Channel.CONFLATED)
    val closeEvents: Flow<Unit> = closeChannel.receiveAsFlow()
    // ===== END SECTION: EXAM_EDITOR_FIELDS =====

    init {
        if (!isNew) {
            viewModelScope.launch {
                val exam = exams.getExam(examId)
                if (exam == null) {
                    closeChannel.send(Unit)
                    return@launch
                }
                titleState.setTextAndPlaceCursorAtEnd(exam.title)
                notesState.setTextAndPlaceCursorAtEnd(exam.notes)
                kind = exam.kind
                subjectId = exam.subject?.id
                date = exam.date
                time = exam.time
                original = currentDraft()
                isLoaded = true
            }
        }
    }

    // ===== SECTION: EXAM_EDITOR_ACTIONS =====
    val canSave: Boolean get() = isLoaded && titleState.text.isNotBlank()

    val hasChanges: Boolean get() = isLoaded && currentDraft() != original

    fun save() {
        if (!canSave || isSaving) return
        isSaving = true
        val draft = currentDraft()
        viewModelScope.launch {
            exams.save(examId, draft)
            closeChannel.send(Unit)
        }
    }

    fun delete() {
        if (isNew || isSaving) return
        isSaving = true
        actions.deleteExam(examId, original.title)
        closeChannel.trySend(Unit)
    }

    private fun currentDraft(): ExamDraft = ExamDraft(
        title = titleState.text.toString().trim(),
        kind = kind,
        subjectId = subjectId,
        date = date.toString(),
        time = time?.let { formatMinute(it.hour * 60 + it.minute) },
        notes = notesState.text.toString().trim(),
    )
    // ===== END SECTION: EXAM_EDITOR_ACTIONS =====

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(examId: Long, presetSubjectId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                ExamEditorViewModel(
                    examId = examId,
                    presetSubjectId = presetSubjectId,
                    exams = container.examRepository,
                    subjectRepository = container.subjectRepository,
                    actions = container.academicActions,
                    timeProvider = container.timeProvider,
                )
            }
        }
    }
}
