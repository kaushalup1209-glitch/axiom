package com.kaushalkr.axiom.feature.tasks

import com.kaushalkr.axiom.feature.tasks.TaskListBuilder.toItem
import com.kaushalkr.axiom.testing.testTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// ===== SECTION: TEST_TASK_LIST_REORDER =====
class TaskListReorderTest {

    private val today = LocalDate.of(2026, 11, 24)
    private fun item(id: Long) = testTask(id).toItem(today)

    private val sections = listOf(
        TaskSection("a", TaskSectionKind.TOMORROW, null, listOf(item(1), item(2), item(3))),
        TaskSection("b", TaskSectionKind.NO_DATE, null, listOf(item(4), item(5))),
    )

    @Test
    fun moveStaysInsideItsSection() {
        val moved = TaskListReorder.move(sections, fromId = 3, toId = 1)
        assertEquals(listOf(3L, 1L, 2L), moved[0].items.map { it.id })
        assertEquals(sections[1], moved[1])
        assertTrue(TaskListReorder.inSameSection(sections, 1, 3))
        assertFalse(TaskListReorder.inSameSection(sections, 1, 4))
        // A cross-section move changes nothing.
        assertEquals(sections, TaskListReorder.move(sections, fromId = 1, toId = 4))
    }

    @Test
    fun placementReportsNeighbours() {
        val placement = TaskListReorder.placementOf(sections, 2)!!
        assertEquals(1, placement.index)
        assertEquals(1L, placement.previous?.id)
        assertEquals(3L, placement.next?.id)
    }

    @Test
    fun shiftedMovesOneStepAndStopsAtEdges() {
        val down = TaskListReorder.shifted(sections, 1, 1)!!
        assertEquals(1, down.index)
        assertEquals(2L, down.previous?.id)
        assertEquals(3L, down.next?.id)
        assertNull(TaskListReorder.shifted(sections, 1, -1))
        assertNull(TaskListReorder.shifted(sections, 5, 1))
    }
}
// ===== END SECTION: TEST_TASK_LIST_REORDER =====
