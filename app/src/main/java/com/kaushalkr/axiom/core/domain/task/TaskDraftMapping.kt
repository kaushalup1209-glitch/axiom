package com.kaushalkr.axiom.core.domain.task

import com.kaushalkr.axiom.core.model.SubtaskDraft
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.core.model.TaskDraft

// ===== SECTION: TASK_DRAFT_MAPPING =====
/** The editable form of a stored task. */
fun Task.toDraft(): TaskDraft = TaskDraft(
    title = title,
    description = description,
    subject = subject,
    priority = priority,
    dueDate = dueDate?.toString(),
    dueTime = dueTime?.toString(),
    estimatedMinutes = estimatedMinutes,
    recurrence = recurrence,
    subtasks = subtasks.map { SubtaskDraft(title = it.title, isDone = it.isDone, id = it.id) },
)
// ===== END SECTION: TASK_DRAFT_MAPPING =====
