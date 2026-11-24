package com.kaushalkr.axiom.core.domain

import androidx.annotation.StringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// ===== SECTION: UNDO_EVENT =====
/** A reversible change offered to the user, e.g. "Task deleted · Undo". */
class UndoEvent internal constructor(
    @param:StringRes val messageRes: Int,
    val messageArgument: String?,
    internal val undo: suspend () -> Unit,
)
// ===== END SECTION: UNDO_EVENT =====

// ===== SECTION: UNDO_CONTROLLER =====
/**
 * App-wide undo. Actions run in an application scope, so an undo offered from a screen that
 * closes (e.g. deleting from the task editor) still works from the snackbar on the next screen.
 * Only the most recent event is shown; older ones expire.
 */
class UndoController(private val applicationScope: CoroutineScope) {

    private val mutableEvents = MutableSharedFlow<UndoEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UndoEvent> = mutableEvents.asSharedFlow()

    fun offer(@StringRes messageRes: Int, messageArgument: String? = null, undo: suspend () -> Unit) {
        mutableEvents.tryEmit(UndoEvent(messageRes, messageArgument, undo))
    }

    fun undo(event: UndoEvent) {
        applicationScope.launch { event.undo() }
    }
}
// ===== END SECTION: UNDO_CONTROLLER =====
