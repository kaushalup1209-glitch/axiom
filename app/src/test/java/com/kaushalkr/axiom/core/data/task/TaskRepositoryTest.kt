package com.kaushalkr.axiom.core.data.task

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.core.domain.task.toDraft
import com.kaushalkr.axiom.core.model.SubtaskDraft
import com.kaushalkr.axiom.core.model.TaskDraft
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.model.TaskRecurrence
import com.kaushalkr.axiom.testing.FakeTimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: TEST_TASK_REPOSITORY =====
@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {

    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        AxiomDatabase::class.java,
    ).allowMainThreadQueries().build()

    // 2026-11-24 10:00 UTC
    private val time = FakeTimeProvider(now = 1_795_514_400_000L)
    private val repository = TaskRepository(database, time)
    private val today = LocalDate.of(2026, 11, 24)

    @After
    fun closeDatabase() = database.close()

    @Test
    fun saveTrimsFieldsAndKeepsSubtaskOrder() = runTest {
        val id = repository.save(
            0L,
            TaskDraft(
                title = "  Essay draft  ",
                subject = " English ",
                priority = TaskPriority.HIGH,
                dueDate = "2026-11-25",
                dueTime = "16:30",
                estimatedMinutes = 90,
                subtasks = listOf(SubtaskDraft("Outline"), SubtaskDraft("   "), SubtaskDraft("Write")),
            ),
        )
        val task = repository.getTask(id)!!
        assertEquals("Essay draft", task.title)
        assertEquals("English", task.subject)
        assertEquals(LocalDate.of(2026, 11, 25), task.dueDate)
        assertEquals(LocalTime.of(16, 30), task.dueTime)
        assertEquals(90, task.estimatedMinutes)
        assertEquals(listOf("Outline", "Write"), task.subtasks.map { it.title })
        assertEquals(listOf("English"), repository.subjects.first())
    }

    @Test
    fun editingKeepsCreationTimeAndManualPosition() = runTest {
        val id = repository.save(0L, TaskDraft(title = "Read"))
        val original = repository.getTask(id)!!
        time.advanceMinutes(5)
        repository.save(id, original.toDraft().copy(title = "Read chapter 2", subtasks = listOf(SubtaskDraft("Notes", isDone = true))))
        val edited = repository.getTask(id)!!
        assertEquals("Read chapter 2", edited.title)
        assertEquals(original.createdAtMillis, edited.createdAtMillis)
        assertEquals(original.sortOrder, edited.sortOrder, 0.0)
        assertEquals(1, edited.completedSubtasks)
    }

    @Test
    fun repeatingTaskWithoutDateStartsToday() = runTest {
        val id = repository.save(0L, TaskDraft(title = "Flashcards", recurrence = TaskRecurrence.DAILY))
        assertEquals(today, repository.getTask(id)!!.dueDate)
    }

    @Test
    fun completingRepeatingTaskSchedulesNextAndUndoRemovesIt() = runTest {
        val id = repository.save(
            0L,
            TaskDraft(title = "Revise", recurrence = TaskRecurrence.DAILY, dueDate = today.toString(), subtasks = listOf(SubtaskDraft("Cards", isDone = true))),
        )
        val completion = repository.complete(id)!!
        val nextId = completion.spawnedTaskId!!
        val next = repository.getTask(nextId)!!
        assertEquals(today.plusDays(1), next.dueDate)
        assertFalse(next.subtasks.single().isDone)
        assertTrue(repository.getTask(id)!!.isCompleted)
        assertEquals(listOf(nextId), repository.openTasks.first().map { it.id })
        assertEquals(listOf(id), repository.completedSince(today).first().map { it.id })
        // Completing again is a no-op.
        assertNull(repository.complete(id))

        repository.undoCompletion(completion)
        assertNull(repository.getTask(nextId))
        assertFalse(repository.getTask(id)!!.isCompleted)
    }

    @Test
    fun deleteAndRestoreBringsBackTheSameTask() = runTest {
        val id = repository.save(0L, TaskDraft(title = "Lab report", subtasks = listOf(SubtaskDraft("Graphs"), SubtaskDraft("Conclusion"))))
        val before = repository.getTask(id)!!
        val snapshot = repository.delete(id)
        assertNotNull(snapshot)
        assertNull(repository.getTask(id))

        repository.restore(snapshot!!)
        assertEquals(before, repository.getTask(id))
    }

    @Test
    fun moveWritesOnlyTheMovedTaskBetweenNeighbours() = runTest {
        val a = repository.save(0L, TaskDraft(title = "A"))
        val b = repository.save(0L, TaskDraft(title = "B"))
        val c = repository.save(0L, TaskDraft(title = "C"))
        suspend fun orderOf(id: Long) = repository.getTask(id)!!.sortOrder

        repository.move(c, previousSortOrder = orderOf(a), nextSortOrder = orderOf(b))
        assertEquals(listOf(a, c, b), repository.openTasks.first().map { it.id })

        repository.move(a, previousSortOrder = orderOf(b), nextSortOrder = null)
        assertEquals(listOf(c, b, a), repository.openTasks.first().map { it.id })
    }
}
// ===== END SECTION: TEST_TASK_REPOSITORY =====
