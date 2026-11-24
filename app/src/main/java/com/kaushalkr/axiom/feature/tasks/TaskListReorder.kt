package com.kaushalkr.axiom.feature.tasks

// ===== SECTION: TASK_PLACEMENT =====
/** Where a task sits in its section, with the neighbours that define its manual order. */
data class TaskPlacement(
    val item: TaskListItem,
    val index: Int,
    val previous: TaskListItem?,
    val next: TaskListItem?,
)
// ===== END SECTION: TASK_PLACEMENT =====

// ===== SECTION: TASK_LIST_REORDER =====
/**
 * Pure helpers for drag-to-reorder and the accessibility move actions. Moves never cross a
 * section, so a task keeps its date when it is reordered.
 */
object TaskListReorder {

    fun inSameSection(sections: List<TaskSection>, firstId: Long, secondId: Long): Boolean =
        sections.any { section -> section.items.any { it.id == firstId } && section.items.any { it.id == secondId } }

    /** Moves [fromId] to the position of [toId]. Returns [sections] unchanged if that is not possible. */
    fun move(sections: List<TaskSection>, fromId: Long, toId: Long): List<TaskSection> =
        sections.map { section ->
            val from = section.items.indexOfFirst { it.id == fromId }
            val to = section.items.indexOfFirst { it.id == toId }
            if (from < 0 || to < 0 || from == to) {
                section
            } else {
                section.copy(items = section.items.toMutableList().apply { add(to, removeAt(from)) })
            }
        }

    fun placementOf(sections: List<TaskSection>, id: Long): TaskPlacement? {
        for (section in sections) {
            val index = section.items.indexOfFirst { it.id == id }
            if (index >= 0) {
                return TaskPlacement(
                    item = section.items[index],
                    index = index,
                    previous = section.items.getOrNull(index - 1),
                    next = section.items.getOrNull(index + 1),
                )
            }
        }
        return null
    }

    /**
     * The placement [id] would have after moving [offset] places (-1 up, +1 down) within its
     * section, or null at the section edge.
     */
    fun shifted(sections: List<TaskSection>, id: Long, offset: Int): TaskPlacement? {
        val section = sections.firstOrNull { section -> section.items.any { it.id == id } } ?: return null
        val from = section.items.indexOfFirst { it.id == id }
        val to = from + offset
        if (to !in section.items.indices) return null
        val reordered = section.items.toMutableList().apply { add(to, removeAt(from)) }
        return TaskPlacement(
            item = reordered[to],
            index = to,
            previous = reordered.getOrNull(to - 1),
            next = reordered.getOrNull(to + 1),
        )
    }
}
// ===== END SECTION: TASK_LIST_REORDER =====
