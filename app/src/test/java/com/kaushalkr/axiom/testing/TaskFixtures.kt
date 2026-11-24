package com.kaushalkr.axiom.testing

import com.kaushalkr.axiom.core.model.Subtask
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.model.TaskRecurrence
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: TEST_TASK_FIXTURES =====
/** Builds a [Task] for pure list tests; only the fields a test cares about need setting. */
fun testTask(
    id: Long,
    title: String = "Task $id",
    dueDate: LocalDate? = null,
    dueTime: LocalTime? = null,
    priority: TaskPriority = TaskPriority.MEDIUM,
    subject: String = "",
    description: String = "",
    completedAtMillis: Long? = null,
    createdAtMillis: Long = id,
    sortOrder: Double = id.toDouble(),
    subtasks: List<String> = emptyList(),
): Task = Task(
    id = id,
    title = title,
    description = description,
    subject = subject,
    priority = priority,
    dueDate = dueDate,
    dueTime = dueTime,
    estimatedMinutes = null,
    recurrence = TaskRecurrence.NONE,
    completedAtMillis = completedAtMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = createdAtMillis,
    sortOrder = sortOrder,
    subtasks = subtasks.mapIndexed { index, subtask -> Subtask(id = id * 100 + index, title = subtask, isDone = false, position = index) },
)
// ===== END SECTION: TEST_TASK_FIXTURES =====
