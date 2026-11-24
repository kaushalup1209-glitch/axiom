package com.kaushalkr.axiom.core.data.academic

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kaushalkr.axiom.core.model.Exam
import com.kaushalkr.axiom.core.model.ExamKind
import com.kaushalkr.axiom.core.model.SubjectRef
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: EXAM_ENTITY =====
@Entity(
    tableName = "exams",
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
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "subject_id") val subjectId: Long?,
    /** Epoch day. */
    @ColumnInfo(name = "date") val epochDay: Long,
    /** Minute of day (0..1439), or null for an all-day deadline. */
    @ColumnInfo(name = "time") val minuteOfDay: Int?,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "created_at") val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at") val updatedAtMillis: Long,
)
// ===== END SECTION: EXAM_ENTITY =====

// ===== SECTION: EXAM_ENTITY_MAPPING =====
fun ExamEntity.toModel(subjects: Map<Long, SubjectRef>): Exam = Exam(
    id = id,
    title = title,
    kind = ExamKind.entries.firstOrNull { it.name == kind } ?: ExamKind.EXAM,
    subject = subjectId?.let { subjects[it] },
    date = LocalDate.ofEpochDay(epochDay),
    time = minuteOfDay?.let { LocalTime.of(it / 60, it % 60) },
    notes = notes,
    createdAtMillis = createdAtMillis,
)
// ===== END SECTION: EXAM_ENTITY_MAPPING =====
