package com.kaushalkr.axiom.feature.academics.subject

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Immutable
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
import com.kaushalkr.axiom.core.domain.academic.ExamCountdown
import com.kaushalkr.axiom.core.model.Chapter
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.core.model.SubjectDetail
import com.kaushalkr.axiom.feature.academics.SubjectSaveResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

// ===== SECTION: SUBJECT_DETAIL_UI_STATE =====
@Immutable
data class SubjectDetailUiState(
    val detail: SubjectDetail,
    val openTaskCount: Int,
    val upcomingExams: List<Exam>,
    val today: LocalDate,
)
// ===== END SECTION: SUBJECT_DETAIL_UI_STATE =====

/** One subject's syllabus: chapters to tick off, its open tasks and upcoming exams. */
@OptIn(ExperimentalCoroutinesApi::class)
class SubjectDetailViewModel(
    private val subjectId: Long,
    private val subjects: SubjectRepository,
    exams: ExamRepository,
    private val actions: AcademicActions,
) : ViewModel() {

    // ===== SECTION: SUBJECT_DETAIL_VM_STATE =====
    val newChaptersState = TextFieldState()
    private val today = LocalDate.now()

    private val detail = subjects.observeDetail(subjectId)

    private val openTaskCount = detail
        .map { it?.subject?.name }
        .distinctUntilChanged()
        .flatMapLatest { name -> if (name == null) flowOf(0) else subjects.observeOpenTaskCount(name) }

    /** Null while loading; [isMissing] turns true if the subject was deleted. */
    val uiState: StateFlow<SubjectDetailUiState?> = combine(detail, openTaskCount, exams.exams) { detail, count, examList ->
        detail?.let {
            SubjectDetailUiState(
                detail = it,
                openTaskCount = count,
                upcomingExams = ExamCountdown.upcoming(examList.filter { exam -> exam.subject?.id == subjectId }, today),
                today = today,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initialValue = null)

    val isMissing: Flow<Boolean> = detail.map { it == null }.distinctUntilChanged()

    private val closeChannel = Channel<Unit>(Channel.CONFLATED)
    val closeEvents: Flow<Unit> = closeChannel.receiveAsFlow()
    // ===== END SECTION: SUBJECT_DETAIL_VM_STATE =====

    // ===== SECTION: SUBJECT_DETAIL_VM_ACTIONS =====
    /** Adds one chapter per line of the input field. */
    fun addChapters() {
        val lines = newChaptersState.text.toString().lines()
        if (lines.all { it.isBlank() }) return
        newChaptersState.clearText()
        viewModelScope.launch { subjects.addChapters(subjectId, lines) }
    }

    fun toggleChapter(chapter: Chapter) {
        actions.setChapterDone(chapter.id, !chapter.isDone)
    }

    fun moveChapter(chapter: Chapter, offset: Int) {
        actions.moveChapter(chapter.id, offset)
    }

    fun deleteChapter(chapter: Chapter) {
        actions.deleteChapter(chapter.id, chapter.title)
    }

    suspend fun updateSubject(name: String, colorIndex: Int): SubjectSaveResult {
        val cleaned = name.trim()
        if (cleaned.isEmpty()) return SubjectSaveResult.BLANK
        return if (subjects.update(subjectId, cleaned, colorIndex)) SubjectSaveResult.SAVED else SubjectSaveResult.DUPLICATE
    }

    fun deleteSubject() {
        val name = uiState.value?.detail?.subject?.name ?: return
        actions.deleteSubject(subjectId, name)
        closeChannel.trySend(Unit)
    }
    // ===== END SECTION: SUBJECT_DETAIL_VM_ACTIONS =====

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(subjectId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                SubjectDetailViewModel(subjectId, container.subjectRepository, container.examRepository, container.academicActions)
            }
        }
    }
}
