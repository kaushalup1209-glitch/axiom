package com.kaushalkr.axiom.core.data.academic

import androidx.room.withTransaction
import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.core.model.AcademicLimits
import com.kaushalkr.axiom.core.model.Chapter
import com.kaushalkr.axiom.core.model.Subject
import com.kaushalkr.axiom.core.model.SubjectDetail
import com.kaushalkr.axiom.core.model.SubjectRef
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// ===== SECTION: SUBJECT_SNAPSHOTS =====
/** Everything a subject deletion removed or detached, so undo puts it back exactly. */
data class SubjectSnapshot(
    val subject: SubjectEntity,
    val chapters: List<ChapterEntity>,
    val blockIds: List<Long>,
    val examIds: List<Long>,
)
// ===== END SECTION: SUBJECT_SNAPSHOTS =====

/**
 * Subjects and their syllabus (chapters). Tasks keep their subject as text, so a rename is
 * applied to matching tasks in the same transaction.
 */
class SubjectRepository(
    private val database: AxiomDatabase,
    private val timeProvider: TimeProvider,
) {
    private val dao = database.subjectDao()

    // ===== SECTION: SUBJECT_REPOSITORY_READ =====
    val subjects: Flow<List<Subject>> =
        dao.observeWithCounts().map { rows -> rows.map { it.toModel() } }.distinctUntilChanged()

    /** Lightweight references for pickers and joins. */
    val subjectRefs: Flow<List<SubjectRef>> =
        dao.observeAll().map { rows -> rows.map { it.toRef() } }.distinctUntilChanged()

    fun observeDetail(id: Long): Flow<SubjectDetail?> =
        combine(subjects, dao.observeChapters(id)) { all, chapters ->
            all.firstOrNull { it.id == id }?.let { subject -> SubjectDetail(subject, chapters.map { it.toModel() }) }
        }.distinctUntilChanged()

    fun observeOpenTaskCount(name: String): Flow<Int> = dao.observeOpenTaskCount(name).distinctUntilChanged()

    suspend fun getSubject(id: Long): SubjectRef? = dao.get(id)?.toRef()

    suspend fun findByName(name: String): SubjectRef? = dao.findByName(name.trim())?.toRef()

    suspend fun getChapter(id: Long): Chapter? = dao.getChapter(id)?.toModel()
    // ===== END SECTION: SUBJECT_REPOSITORY_READ =====

    // ===== SECTION: SUBJECT_REPOSITORY_WRITE =====
    /**
     * Creates a subject, or returns the existing one with the same name (names are unique,
     * ignoring case). Returns null for a blank name.
     */
    suspend fun create(name: String, colorIndex: Int? = null): Long? {
        val cleaned = name.trim().take(AcademicLimits.SUBJECT_NAME_MAX_LENGTH)
        if (cleaned.isEmpty()) return null
        return database.withTransaction {
            dao.findByName(cleaned)?.id ?: dao.insertSubject(
                SubjectEntity(
                    name = cleaned,
                    colorIndex = (colorIndex ?: dao.getAll().size).mod(AcademicLimits.SUBJECT_COLOR_COUNT),
                    createdAtMillis = timeProvider.nowMillis(),
                ),
            )
        }
    }

    /** Renames or recolours a subject; returns false if the new name belongs to another subject. */
    suspend fun update(id: Long, name: String, colorIndex: Int): Boolean = database.withTransaction {
        val existing = dao.get(id) ?: return@withTransaction false
        val cleaned = name.trim().take(AcademicLimits.SUBJECT_NAME_MAX_LENGTH)
        if (cleaned.isEmpty()) return@withTransaction false
        val clash = dao.findByName(cleaned)
        if (clash != null && clash.id != id) return@withTransaction false
        dao.updateSubject(existing.copy(name = cleaned, colorIndex = colorIndex.mod(AcademicLimits.SUBJECT_COLOR_COUNT)))
        if (existing.name != cleaned) dao.renameTaskSubjects(existing.name, cleaned)
        true
    }

    suspend fun delete(id: Long): SubjectSnapshot? = database.withTransaction {
        val subject = dao.get(id) ?: return@withTransaction null
        val snapshot = SubjectSnapshot(
            subject = subject,
            chapters = dao.getChapters(id),
            blockIds = dao.blockIdsFor(id),
            examIds = dao.examIdsFor(id),
        )
        dao.deleteSubject(id)
        snapshot
    }

    suspend fun restore(snapshot: SubjectSnapshot) {
        dao.restore(snapshot.subject, snapshot.chapters, snapshot.blockIds, snapshot.examIds)
    }
    // ===== END SECTION: SUBJECT_REPOSITORY_WRITE =====

    // ===== SECTION: SUBJECT_REPOSITORY_CHAPTERS =====
    /** Appends chapters in order, skipping blank lines; returns how many were added. */
    suspend fun addChapters(subjectId: Long, titles: List<String>): Int = database.withTransaction {
        if (dao.get(subjectId) == null) return@withTransaction 0
        val existingCount = dao.getChapters(subjectId).size
        val room = (AcademicLimits.MAX_CHAPTERS_PER_SUBJECT - existingCount).coerceAtLeast(0)
        val cleaned = titles
            .map { it.trim().take(AcademicLimits.CHAPTER_TITLE_MAX_LENGTH) }
            .filter { it.isNotEmpty() }
            .take(room)
        if (cleaned.isEmpty()) return@withTransaction 0
        var position = dao.maxChapterPosition(subjectId)
        cleaned.forEach { title ->
            position += 1
            dao.insertChapter(ChapterEntity(subjectId = subjectId, title = title, isDone = false, completedAtMillis = null, position = position))
        }
        cleaned.size
    }

    suspend fun setChapterDone(chapterId: Long, isDone: Boolean) {
        dao.setChapterDone(chapterId, isDone, if (isDone) timeProvider.nowMillis() else null)
    }

    suspend fun renameChapter(chapterId: Long, title: String) {
        val cleaned = title.trim().take(AcademicLimits.CHAPTER_TITLE_MAX_LENGTH)
        if (cleaned.isNotEmpty()) dao.renameChapter(chapterId, cleaned)
    }

    /** Swaps a chapter with its neighbour; [offset] is -1 (up) or +1 (down). */
    suspend fun moveChapter(chapterId: Long, offset: Int) = database.withTransaction {
        val chapter = dao.getChapter(chapterId) ?: return@withTransaction
        val chapters = dao.getChapters(chapter.subjectId)
        val index = chapters.indexOfFirst { it.id == chapterId }
        val target = index + offset
        if (index < 0 || target !in chapters.indices) return@withTransaction
        // Rewrite positions densely so ties from older data cannot block the swap.
        val reordered = chapters.toMutableList().apply { add(target, removeAt(index)) }
        reordered.forEachIndexed { position, item ->
            if (item.position != position) dao.setChapterPosition(item.id, position)
        }
    }

    suspend fun deleteChapter(chapterId: Long): ChapterEntity? = database.withTransaction {
        val chapter = dao.getChapter(chapterId) ?: return@withTransaction null
        dao.deleteChapter(chapterId)
        chapter
    }

    suspend fun restoreChapter(chapter: ChapterEntity) {
        dao.insertChapters(listOf(chapter))
    }
    // ===== END SECTION: SUBJECT_REPOSITORY_CHAPTERS =====
}
