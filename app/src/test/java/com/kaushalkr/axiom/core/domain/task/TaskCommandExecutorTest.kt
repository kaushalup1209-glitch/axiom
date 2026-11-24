package com.kaushalkr.axiom.core.domain.task

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.domain.UndoController
import com.kaushalkr.axiom.core.domain.UndoEvent
import com.kaushalkr.axiom.core.model.TaskDraft
import com.kaushalkr.axiom.core.model.TaskRecurrence
import com.kaushalkr.axiom.testing.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

// ===== SECTION: TEST_TASK_COMMAND_EXECUTOR =====
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TaskCommandExecutorTest {

    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        AxiomDatabase::class.java,
    ).allowMainThreadQueries().build()

    private val time = FakeTimeProvider(now = 1_795_514_400_000L)
    private val repository = TaskRepository(database, time)

    @After
    fun closeDatabase() = database.close()

    private class Harness(val executor: TaskCommandExecutor, val undo: UndoController, val offered: List<UndoEvent>)

    private fun TestScope.harness(): Harness {
        val undo = UndoController(backgroundScope)
        val offered = mutableListOf<UndoEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { undo.events.collect { offered += it } }
        val executor = TaskCommandExecutor(repository, TaskActions(repository, undo, backgroundScope))
        return Harness(executor, undo, offered)
    }

    @Test
    fun createValidatesInput() = runTest {
        val executor = harness().executor
        assertTrue(executor.execute(TaskCommand.Create(TaskDraft(title = " "))) is TaskCommandResult.Invalid)
        assertTrue(executor.execute(TaskCommand.Create(TaskDraft(title = "Read", dueDate = "next friday"))) is TaskCommandResult.Invalid)
        assertTrue(executor.execute(TaskCommand.Create(TaskDraft(title = "Read", dueTime = "25:99"))) is TaskCommandResult.Invalid)

        val saved = executor.execute(TaskCommand.Create(TaskDraft(title = "Read", dueDate = "2026-11-26")))
        assertTrue(saved is TaskCommandResult.Saved)
        val listed = executor.execute(TaskCommand.ListOpen) as TaskCommandResult.OpenTasks
        assertEquals("2026-11-26", listed.tasks.single().dueDate)
    }

    @Test
    fun completeAndDeleteOfferUndo() = runTest {
        val harness = harness()
        val id = (harness.executor.execute(TaskCommand.Create(TaskDraft(title = "Quiz", recurrence = TaskRecurrence.WEEKLY))) as TaskCommandResult.Saved).taskId

        val completed = harness.executor.execute(TaskCommand.Complete(id)) as TaskCommandResult.Completed
        assertNotNull(completed.nextOccurrenceId)
        assertEquals(R.string.undo_task_completed_repeating, harness.offered.last().messageRes)

        val deleted = harness.executor.execute(TaskCommand.Delete(completed.nextOccurrenceId!!))
        assertTrue(deleted is TaskCommandResult.Deleted)
        assertEquals(R.string.undo_task_deleted, harness.offered.last().messageRes)

        harness.undo.undo(harness.offered.last())
        advanceUntilIdle()
        assertEquals(listOf(completed.nextOccurrenceId), repository.openTasks.first().map { it.id })
    }

    @Test
    fun unknownTasksAreReported() = runTest {
        val executor = harness().executor
        assertEquals(TaskCommandResult.NotFound(99), executor.execute(TaskCommand.Complete(99)))
        assertEquals(TaskCommandResult.NotFound(99), executor.execute(TaskCommand.Delete(99)))
        assertEquals(TaskCommandResult.NotFound(99), executor.execute(TaskCommand.Reschedule(99, "2026-12-01")))
        assertEquals(TaskCommandResult.NotFound(99), executor.execute(TaskCommand.Update(99, TaskDraft(title = "X"))))
    }

    @Test
    fun rescheduleChangesOnlyTheDate() = runTest {
        val executor = harness().executor
        val id = (executor.execute(TaskCommand.Create(TaskDraft(title = "Essay", subject = "English"))) as TaskCommandResult.Saved).taskId
        executor.execute(TaskCommand.Reschedule(id, dueDate = "2026-12-01", dueTime = "09:00"))
        val task = repository.getTask(id)!!
        assertEquals("2026-12-01", task.dueDate.toString())
        assertEquals("09:00", task.dueTime.toString())
        assertEquals("English", task.subject)
        assertFalse(task.isCompleted)
    }

    @Test
    fun commandsRoundTripAsAssistantJson() {
        val json = Json { classDiscriminator = "type" }
        val command: TaskCommand = TaskCommand.Create(TaskDraft(title = "Read", dueDate = "2026-11-26"))
        val encoded = json.encodeToString(TaskCommand.serializer(), command)
        assertTrue(encoded.contains("\"type\":\"create_task\""))
        assertEquals(command, json.decodeFromString(TaskCommand.serializer(), encoded))
        assertEquals(TaskCommand.ListOpen, json.decodeFromString(TaskCommand.serializer(), """{"type":"list_open_tasks"}"""))
    }
}
// ===== END SECTION: TEST_TASK_COMMAND_EXECUTOR =====
