package com.kaushalkr.axiom.core.data.routine

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ===== SECTION: ROUTINE_DAO =====
@Dao
interface RoutineDao {

    // ===== SECTION: ROUTINE_DAO_READ =====
    @Query("SELECT * FROM routine_blocks ORDER BY start_minute ASC, id ASC")
    fun observeBlocks(): Flow<List<RoutineBlockEntity>>

    @Query(
        """
        SELECT l.block_id AS block_id, t.id AS task_id, t.title AS title, t.completed_at AS completed_at
        FROM routine_task_links l INNER JOIN tasks t ON t.id = l.task_id
        ORDER BY t.completed_at IS NOT NULL, t.sort_order ASC
        """,
    )
    fun observeLinkedTasks(): Flow<List<LinkedTaskRow>>

    @Query("SELECT * FROM routine_blocks WHERE id = :id")
    suspend fun get(id: Long): RoutineBlockEntity?

    @Query("SELECT task_id FROM routine_task_links WHERE block_id = :blockId")
    suspend fun linkedTaskIds(blockId: Long): List<Long>
    // ===== END SECTION: ROUTINE_DAO_READ =====

    // ===== SECTION: ROUTINE_DAO_WRITE =====
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(block: RoutineBlockEntity): Long

    @Update
    suspend fun update(block: RoutineBlockEntity)

    @Query("DELETE FROM routine_blocks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM routine_task_links WHERE block_id = :blockId")
    suspend fun clearLinks(blockId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLinks(links: List<RoutineTaskLinkEntity>)

    @Query("UPDATE routine_blocks SET start_minute = :startMinute, updated_at = :updatedAtMillis WHERE id = :id")
    suspend fun setStart(id: Long, startMinute: Int, updatedAtMillis: Long)

    /** Inserts or replaces a block and its task links in one transaction. Returns the block id. */
    @Transaction
    suspend fun replace(block: RoutineBlockEntity, taskIds: List<Long>): Long {
        val id = if (block.id == 0L) insert(block) else block.id.also { update(block) }
        clearLinks(id)
        if (taskIds.isNotEmpty()) insertLinks(taskIds.map { RoutineTaskLinkEntity(blockId = id, taskId = it) })
        return id
    }

    /** Puts back a deleted block with its original id and links, so undo is exact. */
    @Transaction
    suspend fun restore(block: RoutineBlockEntity, taskIds: List<Long>) {
        insert(block)
        if (taskIds.isNotEmpty()) insertLinks(taskIds.map { RoutineTaskLinkEntity(blockId = block.id, taskId = it) })
    }
    // ===== END SECTION: ROUTINE_DAO_WRITE =====
}
// ===== END SECTION: ROUTINE_DAO =====
