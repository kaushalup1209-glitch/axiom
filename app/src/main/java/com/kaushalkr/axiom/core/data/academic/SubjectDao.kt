package com.kaushalkr.axiom.core.data.academic

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ===== SECTION: SUBJECT_DAO =====
@Dao
interface SubjectDao {

    // ===== SECTION: SUBJECT_DAO_READ =====
    @Query(
        """
        SELECT s.*, COALESCE(SUM(c.is_done), 0) AS chapters_done, COUNT(c.id) AS chapters_total
        FROM subjects s LEFT JOIN chapters c ON c.subject_id = s.id
        GROUP BY s.id
        ORDER BY s.name COLLATE NOCASE
        """,
    )
    fun observeWithCounts(): Flow<List<SubjectWithCounts>>

    @Query("SELECT * FROM subjects ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun get(id: Long): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE id = :id")
    fun observe(id: Long): Flow<SubjectEntity?>

    @Query("SELECT * FROM subjects WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): SubjectEntity?

    @Query("SELECT * FROM chapters WHERE subject_id = :subjectId ORDER BY position ASC, id ASC")
    fun observeChapters(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE subject_id = :subjectId ORDER BY position ASC, id ASC")
    suspend fun getChapters(subjectId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapter(id: Long): ChapterEntity?

    @Query("SELECT COALESCE(MAX(position), -1) FROM chapters WHERE subject_id = :subjectId")
    suspend fun maxChapterPosition(subjectId: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE completed_at IS NULL AND subject = :name COLLATE NOCASE")
    fun observeOpenTaskCount(name: String): Flow<Int>

    @Query("SELECT id FROM routine_blocks WHERE subject_id = :subjectId")
    suspend fun blockIdsFor(subjectId: Long): List<Long>

    @Query("SELECT id FROM exams WHERE subject_id = :subjectId")
    suspend fun examIdsFor(subjectId: Long): List<Long>
    // ===== END SECTION: SUBJECT_DAO_READ =====

    // ===== SECTION: SUBJECT_DAO_WRITE =====
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("UPDATE chapters SET is_done = :isDone, completed_at = :completedAtMillis WHERE id = :id")
    suspend fun setChapterDone(id: Long, isDone: Boolean, completedAtMillis: Long?)

    @Query("UPDATE chapters SET title = :title WHERE id = :id")
    suspend fun renameChapter(id: Long, title: String)

    @Query("UPDATE chapters SET position = :position WHERE id = :id")
    suspend fun setChapterPosition(id: Long, position: Int)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapter(id: Long)

    /** Tasks name their subject as text; keep them attached when a subject is renamed. */
    @Query("UPDATE tasks SET subject = :newName WHERE subject = :oldName COLLATE NOCASE")
    suspend fun renameTaskSubjects(oldName: String, newName: String)

    @Query("UPDATE routine_blocks SET subject_id = :subjectId WHERE id IN (:blockIds)")
    suspend fun relinkBlocks(subjectId: Long, blockIds: List<Long>)

    @Query("UPDATE exams SET subject_id = :subjectId WHERE id IN (:examIds)")
    suspend fun relinkExams(subjectId: Long, examIds: List<Long>)

    /** Puts back a deleted subject with its original ids, chapters and links. */
    @Transaction
    suspend fun restore(subject: SubjectEntity, chapters: List<ChapterEntity>, blockIds: List<Long>, examIds: List<Long>) {
        insertSubject(subject)
        if (chapters.isNotEmpty()) insertChapters(chapters)
        if (blockIds.isNotEmpty()) relinkBlocks(subject.id, blockIds)
        if (examIds.isNotEmpty()) relinkExams(subject.id, examIds)
    }
    // ===== END SECTION: SUBJECT_DAO_WRITE =====
}
// ===== END SECTION: SUBJECT_DAO =====
