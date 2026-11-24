package com.kaushalkr.axiom.core.domain.task

import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.data.task.TaskCompletion
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.domain.UndoController
import com.kaushalkr.axiom.core.model.TaskDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ===== SECTION: TASK_ACTIONS =====
/**
 * User-facing task operations shared by every screen and by [TaskCommandExecutor].
 * Changes run in the application scope, so they finish even if the screen that started them
 * closes, and destructive ones offer undo through [UndoController].
 */
class TaskActions(
    private val repository: TaskRepository,
    private val undoController: UndoController,
    private val applicationScope: CoroutineScope,
) {

    fun complete(taskId: Long, title: String, offerUndo: Boolean = true) {
        applicationScope.launch { completeNow(taskId, title, offerUndo) }
    }

    fun reopen(taskId: Long) {
        applicationScope.launch { repository.reopen(taskId) }
    }

    fun delete(taskId: Long, title: String, offerUndo: Boolean = true) {
        applicationScope.launch { deleteNow(taskId, title, offerUndo) }
    }

    fun setSubtaskDone(subtaskId: Long, isDone: Boolean) {
        applicationScope.launch { repository.setSubtaskDone(subtaskId, isDone) }
    }

    fun move(taskId: Long, previousSortOrder: Double?, nextSortOrder: Double?) {
        applicationScope.launch { repository.move(taskId, previousSortOrder, nextSortOrder) }
    }

    suspend fun save(taskId: Long, draft: TaskDraft): Long = repository.save(taskId, draft)

    // ===== SECTION: TASK_ACTIONS_SUSPENDING =====
    internal suspend fun completeNow(taskId: Long, title: String, offerUndo: Boolean): TaskCompletion? {
        val completion = repository.complete(taskId) ?: return null
        if (offerUndo) {
            val messageRes = if (completion.spawnedTaskId != null) R.string.undo_task_completed_repeating else R.string.undo_task_completed
            undoController.offer(messageRes, title) { repository.undoCompletion(completion) }
        }
        return completion
    }

    internal suspend fun deleteNow(taskId: Long, title: String, offerUndo: Boolean): Boolean {
        val snapshot = repository.delete(taskId) ?: return false
        if (offerUndo) {
            undoController.offer(R.string.undo_task_deleted, title) { repository.restore(snapshot) }
        }
        return true
    }
    // ===== END SECTION: TASK_ACTIONS_SUSPENDING =====
}
// ===== END SECTION: TASK_ACTIONS =====
