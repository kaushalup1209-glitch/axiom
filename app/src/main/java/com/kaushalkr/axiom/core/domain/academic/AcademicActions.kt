package com.kaushalkr.axiom.core.domain.academic

import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.data.academic.ExamRepository
import com.kaushalkr.axiom.core.data.academic.SubjectRepository
import com.kaushalkr.axiom.core.domain.UndoController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ===== SECTION: ACADEMIC_ACTIONS =====
/**
 * Subject, syllabus and exam changes that should outlive the screen that started them.
 * Deletions offer undo through [UndoController].
 */
class AcademicActions(
    private val subjects: SubjectRepository,
    private val exams: ExamRepository,
    private val undoController: UndoController,
    private val applicationScope: CoroutineScope,
) {

    fun deleteSubject(subjectId: Long, name: String) {
        applicationScope.launch {
            val snapshot = subjects.delete(subjectId) ?: return@launch
            undoController.offer(R.string.undo_task_deleted, name) { subjects.restore(snapshot) }
        }
    }

    fun setChapterDone(chapterId: Long, isDone: Boolean) {
        applicationScope.launch { subjects.setChapterDone(chapterId, isDone) }
    }

    fun moveChapter(chapterId: Long, offset: Int) {
        applicationScope.launch { subjects.moveChapter(chapterId, offset) }
    }

    fun deleteChapter(chapterId: Long, title: String) {
        applicationScope.launch {
            val snapshot = subjects.deleteChapter(chapterId) ?: return@launch
            undoController.offer(R.string.undo_task_deleted, title) { subjects.restoreChapter(snapshot) }
        }
    }

    fun deleteExam(examId: Long, title: String) {
        applicationScope.launch {
            val snapshot = exams.delete(examId) ?: return@launch
            undoController.offer(R.string.undo_task_deleted, title) { exams.restore(snapshot) }
        }
    }
}
// ===== END SECTION: ACADEMIC_ACTIONS =====
