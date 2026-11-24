package com.kaushalkr.axiom.feature.academics

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
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.core.model.Subject
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate

// ===== SECTION: ACADEMICS_UI_STATE =====
@Immutable
data class AcademicsUiState(
    val today: LocalDate,
    val subjects: List<Subject>,
    val upcomingExams: List<Exam>,
    val pastExams: List<Exam>,
) {
    val chaptersTotal: Int get() = subjects.sumOf { it.chaptersTotal }
    val chaptersDone: Int get() = subjects.sumOf { it.chaptersDone }
    val syllabusProgress: Float get() = if (chaptersTotal == 0) 0f else chaptersDone.toFloat() / chaptersTotal
}
// ===== END SECTION: ACADEMICS_UI_STATE =====

// ===== SECTION: SUBJECT_SAVE_RESULT =====
/** Outcome of saving a subject from the subject sheet. */
enum class SubjectSaveResult {
    SAVED,
    BLANK,
    DUPLICATE,
}
// ===== END SECTION: SUBJECT_SAVE_RESULT =====

/** Subjects with syllabus progress, and exams split into upcoming and past. */
class AcademicsViewModel(
    private val subjects: SubjectRepository,
    exams: ExamRepository,
    private val actions: AcademicActions,
    timeProvider: TimeProvider,
) : ViewModel() {

    /** Creates a subject; names are unique ignoring case. */
    suspend fun createSubject(name: String, colorIndex: Int): SubjectSaveResult {
        val cleaned = name.trim()
        if (cleaned.isEmpty()) return SubjectSaveResult.BLANK
        if (subjects.findByName(cleaned) != null) return SubjectSaveResult.DUPLICATE
        subjects.create(cleaned, colorIndex)
        return SubjectSaveResult.SAVED
    }

    // ===== SECTION: ACADEMICS_VM_STATE =====
    private val today = timeProvider.ticks(MINUTE_MILLIS)
        .map { Instant.ofEpochMilli(it).atZone(timeProvider.zone()).toLocalDate() }
        .distinctUntilChanged()

    val uiState: StateFlow<AcademicsUiState?> = combine(subjects.subjects, exams.exams, today) { subjectList, examList, today ->
        AcademicsUiState(
            today = today,
            subjects = subjectList,
            upcomingExams = ExamCountdown.upcoming(examList, today),
            pastExams = ExamCountdown.past(examList, today),
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initialValue = null)
    // ===== END SECTION: ACADEMICS_VM_STATE =====

    fun deleteExam(exam: Exam) {
        actions.deleteExam(exam.id, exam.title)
    }

    companion object {
        private const val MINUTE_MILLIS = 60_000L
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = (checkNotNull(this[APPLICATION_KEY]) as AxiomApplication).container
                AcademicsViewModel(container.subjectRepository, container.examRepository, container.academicActions, container.timeProvider)
            }
        }
    }
}
