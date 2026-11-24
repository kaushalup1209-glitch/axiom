package com.kaushalkr.axiom.core.data.academic

import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.core.data.task.toMinuteOfDay
import com.kaushalkr.axiom.core.model.AcademicLimits
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.core.model.ExamDraft
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

// ===== SECTION: EXAM_REPOSITORY =====
/** Exams and academic deadlines, soonest first. */
class ExamRepository(
    database: AxiomDatabase,
    private val timeProvider: TimeProvider,
) {
    private val dao = database.examDao()
    private val subjectDao = database.subjectDao()

    val exams: Flow<List<Exam>> = combine(dao.observeAll(), subjectDao.observeAll()) { exams, subjects ->
        val refs = subjects.associate { it.id to it.toRef() }
        exams.map { it.toModel(refs) }
    }.distinctUntilChanged()

    suspend fun getExam(id: Long): Exam? {
        val entity = dao.get(id) ?: return null
        val subject = entity.subjectId?.let { subjectDao.get(it) }
        return entity.toModel(subject?.let { mapOf(it.id to it.toRef()) }.orEmpty())
    }

    /** Creates ([id] = 0) or replaces an exam. Returns its id. */
    suspend fun save(id: Long, draft: ExamDraft): Long {
        val now = timeProvider.nowMillis()
        val existing = if (id != 0L) dao.get(id) else null
        val subjectId = draft.subjectId?.takeIf { subjectDao.get(it) != null }
        val entity = ExamEntity(
            id = existing?.id ?: 0L,
            title = draft.title.trim().take(AcademicLimits.EXAM_TITLE_MAX_LENGTH),
            kind = draft.kind.name,
            subjectId = subjectId,
            epochDay = draft.parsedDate.toEpochDay(),
            minuteOfDay = draft.parsedTime?.toMinuteOfDay(),
            notes = draft.notes.trim().take(AcademicLimits.EXAM_NOTES_MAX_LENGTH),
            createdAtMillis = existing?.createdAtMillis ?: now,
            updatedAtMillis = now,
        )
        return if (existing == null) dao.insert(entity) else entity.id.also { dao.update(entity) }
    }

    suspend fun delete(id: Long): ExamEntity? {
        val exam = dao.get(id) ?: return null
        dao.delete(id)
        return exam
    }

    /** Puts back a deleted exam; its subject link is dropped if that subject no longer exists. */
    suspend fun restore(exam: ExamEntity) {
        val subjectId = exam.subjectId?.takeIf { subjectDao.get(it) != null }
        dao.insert(exam.copy(subjectId = subjectId))
    }
}
// ===== END SECTION: EXAM_REPOSITORY =====
