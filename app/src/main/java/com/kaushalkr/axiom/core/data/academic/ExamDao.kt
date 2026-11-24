package com.kaushalkr.axiom.core.data.academic

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ===== SECTION: EXAM_DAO =====
@Dao
interface ExamDao {

    /** Soonest first; all-day deadlines after timed ones on the same day. */
    @Query("SELECT * FROM exams ORDER BY date ASC, time IS NULL, time ASC, id ASC")
    fun observeAll(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun get(id: Long): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(exam: ExamEntity): Long

    @Update
    suspend fun update(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun delete(id: Long)
}
// ===== END SECTION: EXAM_DAO =====
