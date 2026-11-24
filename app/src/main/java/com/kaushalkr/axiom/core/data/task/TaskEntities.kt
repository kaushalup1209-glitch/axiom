package com.kaushalkr.axiom.core.data.task

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kaushalkr.axiom.core.model.Subtask
import com.kaushalkr.axiom.core.model.Task
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.model.TaskRecurrence
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: TASK_ENTITY =====
@Entity(
    tableName = "tasks",
    indices = [Index(value = ["completed_at"]), Index(value = ["due_date"])],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "subject") val subject: String,
    @ColumnInfo(name = "priority") val priority: String,
    /** Epoch day of the deadline, or null. */
    @ColumnInfo(name = "due_date") val dueEpochDay: Long?,
    /** Minute of day (0..1439) of the deadline, or null. */
    @ColumnInfo(name = "due_time") val dueMinuteOfDay: Int?,
    @ColumnInfo(name = "estimated_minutes") val estimatedMinutes: Int?,
    @ColumnInfo(name = "recurrence") val recurrence: String,
    @ColumnInfo(name = "completed_at") val completedAtMillis: Long?,
    @ColumnInfo(name = "created_at") val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at") val updatedAtMillis: Long,
    @ColumnInfo(name = "sort_order") val sortOrder: Double,
)
// ===== END SECTION: TASK_ENTITY =====

// ===== SECTION: SUBTASK_ENTITY =====
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["task_id"])],
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "task_id") val taskId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "is_done") val isDone: Boolean,
    @ColumnInfo(name = "position") val position: Int,
)

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(parentColumn = "id", entityColumn = "task_id")
    val subtasks: List<SubtaskEntity>,
)
// ===== END SECTION: SUBTASK_ENTITY =====

// ===== SECTION: TASK_ENTITY_MAPPING =====
fun TaskWithSubtasks.toModel(): Task = Task(
    id = task.id,
    title = task.title,
    description = task.description,
    subject = task.subject,
    priority = TaskPriority.entries.firstOrNull { it.name == task.priority } ?: TaskPriority.MEDIUM,
    dueDate = task.dueEpochDay?.let(LocalDate::ofEpochDay),
    dueTime = task.dueMinuteOfDay?.let { LocalTime.of(it / 60, it % 60) },
    estimatedMinutes = task.estimatedMinutes,
    recurrence = TaskRecurrence.entries.firstOrNull { it.name == task.recurrence } ?: TaskRecurrence.NONE,
    completedAtMillis = task.completedAtMillis,
    createdAtMillis = task.createdAtMillis,
    updatedAtMillis = task.updatedAtMillis,
    sortOrder = task.sortOrder,
    subtasks = subtasks
        .sortedBy { it.position }
        .map { Subtask(id = it.id, title = it.title, isDone = it.isDone, position = it.position) },
)

fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute
// ===== END SECTION: TASK_ENTITY_MAPPING =====
