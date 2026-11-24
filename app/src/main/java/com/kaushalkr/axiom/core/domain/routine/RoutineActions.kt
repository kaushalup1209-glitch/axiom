package com.kaushalkr.axiom.core.domain.routine

import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.data.routine.RoutineRepository
import com.kaushalkr.axiom.core.domain.UndoController
import com.kaushalkr.axiom.core.model.RoutineBlockDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ===== SECTION: ROUTINE_ACTIONS =====
/**
 * Timetable changes shared by the screens and the AI assistant. Writes run in the application
 * scope, and deletions and drag moves offer undo through [UndoController].
 */
class RoutineActions(
    private val repository: RoutineRepository,
    private val undoController: UndoController,
    private val applicationScope: CoroutineScope,
) {

    suspend fun save(blockId: Long, draft: RoutineBlockDraft): Long = repository.save(blockId, draft)

    fun delete(blockId: Long, title: String) {
        applicationScope.launch { deleteNow(blockId, title, offerUndo = true) }
    }

    /** Moves every occurrence of a block; [overlaps] picks the undo message. */
    fun move(blockId: Long, title: String, startMinute: Int, overlaps: Boolean) {
        applicationScope.launch { moveNow(blockId, title, startMinute, overlaps, offerUndo = true) }
    }

    // ===== SECTION: ROUTINE_ACTIONS_SUSPENDING =====
    internal suspend fun deleteNow(blockId: Long, title: String, offerUndo: Boolean): Boolean {
        val snapshot = repository.delete(blockId) ?: return false
        if (offerUndo) undoController.offer(R.string.undo_task_deleted, title) { repository.restore(snapshot) }
        return true
    }

    internal suspend fun moveNow(blockId: Long, title: String, startMinute: Int, overlaps: Boolean, offerUndo: Boolean): Boolean {
        val previous = repository.move(blockId, startMinute) ?: return false
        if (offerUndo && previous != startMinute) {
            val message = if (overlaps) R.string.undo_block_moved_overlap else R.string.undo_block_moved
            undoController.offer(message, title) { repository.move(blockId, previous) }
        }
        return true
    }
    // ===== END SECTION: ROUTINE_ACTIONS_SUSPENDING =====
}
// ===== END SECTION: ROUTINE_ACTIONS =====
