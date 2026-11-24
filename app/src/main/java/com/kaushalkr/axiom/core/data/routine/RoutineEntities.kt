package com.kaushalkr.axiom.core.data.routine

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kaushalkr.axiom.core.data.academic.SubjectEntity
import com.kaushalkr.axiom.core.model.LinkedTask
import com.kaushalkr.axiom.core.model.RoutineAutomation
import com.kaushalkr.axiom.core.model.RoutineBlock
import com.kaushalkr.axiom.core.model.RoutineKind
import com.kaushalkr.axiom.core.model.SubjectRef
import java.time.LocalDate

// ===== SECTION: ROUTINE_BLOCK_ENTITY =====
@Entity(
    tableName = "routine_blocks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index(value = ["subject_id"]), Index(value = ["date"])],
)
data class RoutineBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "subject_id") val subjectId: Long?,
    /** Minutes after midnight, 0..1439. */
    @ColumnInfo(name = "start_minute") val startMinute: Int,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    /** Weekday bit mask (bit 0 = Monday); 0 for a one-off block on [epochDay]. */
    @ColumnInfo(name = "days_mask") val daysMask: Int,
    @ColumnInfo(name = "date") val epochDay: Long?,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "auto_start_focus") val autoStartFocus: Boolean,
    @ColumnInfo(name = "track_distractions") val trackDistractions: Boolean,
    @ColumnInfo(name = "restrict_apps") val restrictApps: Boolean,
    @ColumnInfo(name = "created_at") val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at") val updatedAtMillis: Long,
)
// ===== END SECTION: ROUTINE_BLOCK_ENTITY =====

// ===== SECTION: ROUTINE_TASK_LINK_ENTITY =====
/**
 * Links a block to a to-do task. There is no foreign key to `tasks`: a deleted task simply
 * stops matching (reads join with `tasks`), and undoing the deletion brings the link back.
 */
@Entity(
    tableName = "routine_task_links",
    primaryKeys = ["block_id", "task_id"],
    foreignKeys = [
        ForeignKey(
            entity = RoutineBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["block_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["task_id"])],
)
data class RoutineTaskLinkEntity(
    @ColumnInfo(name = "block_id") val blockId: Long,
    @ColumnInfo(name = "task_id") val taskId: Long,
)

/** A linked task joined with its current title and state. */
data class LinkedTaskRow(
    @ColumnInfo(name = "block_id") val blockId: Long,
    @ColumnInfo(name = "task_id") val taskId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "completed_at") val completedAtMillis: Long?,
)
// ===== END SECTION: ROUTINE_TASK_LINK_ENTITY =====

// ===== SECTION: ROUTINE_ENTITY_MAPPING =====
fun RoutineBlockEntity.toModel(subjects: Map<Long, SubjectRef>, links: List<LinkedTaskRow>): RoutineBlock = RoutineBlock(
    id = id,
    title = title,
    kind = RoutineKind.entries.firstOrNull { it.name == kind } ?: RoutineKind.STUDY,
    subject = subjectId?.let { subjects[it] },
    startMinute = startMinute,
    durationMinutes = durationMinutes,
    daysMask = daysMask,
    date = epochDay?.let(LocalDate::ofEpochDay),
    notes = notes,
    linkedTasks = links.map { LinkedTask(id = it.taskId, title = it.title, isCompleted = it.completedAtMillis != null) },
    automation = RoutineAutomation(
        autoStartFocus = autoStartFocus,
        trackDistractions = trackDistractions,
        restrictApps = restrictApps,
    ),
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
)
// ===== END SECTION: ROUTINE_ENTITY_MAPPING =====
