package com.kaushalkr.axiom.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: MODEL_TASK_PRIORITY =====
/** Task importance, declared from least to most important. */
@Serializable
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
}
// ===== END SECTION: MODEL_TASK_PRIORITY =====

// ===== SECTION: MODEL_TASK_RECURRENCE =====
/** How a task repeats. Completing a repeating task schedules its next occurrence. */
@Serializable
enum class TaskRecurrence {
    NONE,
    DAILY,
    WEEKDAYS,
    WEEKLY,
    MONTHLY,
}
// ===== END SECTION: MODEL_TASK_RECURRENCE =====

// ===== SECTION: MODEL_TASK =====
@Immutable
data class Subtask(
    val id: Long,
    val title: String,
    val isDone: Boolean,
    val position: Int,
)

/** A stored task. [dueTime] is only meaningful when [dueDate] is set. */
@Immutable
data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val subject: String,
    val priority: TaskPriority,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val estimatedMinutes: Int?,
    val recurrence: TaskRecurrence,
    val completedAtMillis: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    /** Manual position; lower comes first. Fractional so a move updates one row. */
    val sortOrder: Double,
    val subtasks: List<Subtask>,
) {
    val isCompleted: Boolean get() = completedAtMillis != null
    val completedSubtasks: Int get() = subtasks.count { it.isDone }
}
// ===== END SECTION: MODEL_TASK =====

// ===== SECTION: MODEL_TASK_DRAFT =====
/** A subtask as entered by the user or an assistant; [id] is 0 for a new subtask. */
@Immutable
@Serializable
data class SubtaskDraft(
    val title: String,
    val isDone: Boolean = false,
    val id: Long = 0,
)

/**
 * Everything needed to create or replace a task. Serializable so the AI assistant
 * (Phase 11) can produce and preview it as structured data before anything is saved.
 */
@Immutable
@Serializable
data class TaskDraft(
    val title: String,
    val description: String = "",
    val subject: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    /** ISO-8601 date (yyyy-MM-dd) or null. */
    val dueDate: String? = null,
    /** ISO-8601 local time (HH:mm) or null. */
    val dueTime: String? = null,
    val estimatedMinutes: Int? = null,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val subtasks: List<SubtaskDraft> = emptyList(),
) {
    val parsedDueDate: LocalDate? get() = dueDate?.let(LocalDate::parse)
    val parsedDueTime: LocalTime? get() = dueTime?.let(LocalTime::parse)
}

object TaskLimits {
    const val TITLE_MAX_LENGTH = 120
    const val DESCRIPTION_MAX_LENGTH = 2_000
    const val SUBJECT_MAX_LENGTH = 40
    const val SUBTASK_MAX_LENGTH = 120
    const val MAX_SUBTASKS = 30
    const val MAX_ESTIMATED_MINUTES = 24 * 60
    val ESTIMATE_PRESETS_MINUTES = listOf(15, 30, 45, 60, 90, 120, 180)
}
// ===== END SECTION: MODEL_TASK_DRAFT =====
