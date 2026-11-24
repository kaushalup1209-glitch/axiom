package com.kaushalkr.axiom.core.data.task

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ===== SECTION: TASK_DAO =====
@Dao
interface TaskDao {

    // ===== SECTION: TASK_DAO_READ =====
    @Transaction
    @Query("SELECT * FROM tasks WHERE completed_at IS NULL ORDER BY sort_order ASC")
    fun observeOpen(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE completed_at IS NOT NULL ORDER BY completed_at DESC LIMIT :limit")
    fun observeCompleted(limit: Int): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE completed_at >= :sinceMillis ORDER BY completed_at DESC")
    fun observeCompletedSince(sinceMillis: Long): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): TaskWithSubtasks?

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observe(id: Long): Flow<TaskWithSubtasks?>

    @Query("SELECT COALESCE(MAX(sort_order), 0) FROM tasks")
    suspend fun maxSortOrder(): Double

    @Query("SELECT DISTINCT subject FROM tasks WHERE subject != '' ORDER BY subject COLLATE NOCASE")
    fun observeSubjects(): Flow<List<String>>
    // ===== END SECTION: TASK_DAO_READ =====

    // ===== SECTION: TASK_DAO_WRITE =====
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Query("DELETE FROM subtasks WHERE task_id = :taskId")
    suspend fun deleteSubtasksOf(taskId: Long)

    @Query("UPDATE tasks SET completed_at = :completedAtMillis, updated_at = :updatedAtMillis WHERE id = :id")
    suspend fun setCompletedAt(id: Long, completedAtMillis: Long?, updatedAtMillis: Long)

    @Query("UPDATE tasks SET sort_order = :sortOrder, updated_at = :updatedAtMillis WHERE id = :id")
    suspend fun setSortOrder(id: Long, sortOrder: Double, updatedAtMillis: Long)

    @Query("UPDATE subtasks SET is_done = :isDone WHERE id = :subtaskId")
    suspend fun setSubtaskDone(subtaskId: Long, isDone: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    /** Inserts or replaces a task and all its subtasks in one transaction. */
    @Transaction
    suspend fun replaceTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>): Long {
        val id = if (task.id == 0L) insertTask(task) else task.id.also { updateTask(task) }
        deleteSubtasksOf(id)
        if (subtasks.isNotEmpty()) insertSubtasks(subtasks.map { it.copy(taskId = id) })
        return id
    }

    /** Restores a deleted task with its original ids, so undo is exact. */
    @Transaction
    suspend fun restore(snapshot: TaskWithSubtasks) {
        insertTask(snapshot.task)
        if (snapshot.subtasks.isNotEmpty()) insertSubtasks(snapshot.subtasks)
    }
    // ===== END SECTION: TASK_DAO_WRITE =====
}
// ===== END SECTION: TASK_DAO =====
