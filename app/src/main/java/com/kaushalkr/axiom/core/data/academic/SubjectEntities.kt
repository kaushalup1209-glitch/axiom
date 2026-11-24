package com.kaushalkr.axiom.core.data.academic

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kaushalkr.axiom.core.model.Chapter
import com.kaushalkr.axiom.core.model.Subject
import com.kaushalkr.axiom.core.model.SubjectRef

// ===== SECTION: SUBJECT_ENTITY =====
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color_index") val colorIndex: Int,
    @ColumnInfo(name = "created_at") val createdAtMillis: Long,
)

/** A subject with its chapter counts, read with one aggregate query. */
data class SubjectWithCounts(
    @Embedded val subject: SubjectEntity,
    @ColumnInfo(name = "chapters_done") val chaptersDone: Int,
    @ColumnInfo(name = "chapters_total") val chaptersTotal: Int,
)
// ===== END SECTION: SUBJECT_ENTITY =====

// ===== SECTION: CHAPTER_ENTITY =====
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["subject_id"])],
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "is_done") val isDone: Boolean,
    @ColumnInfo(name = "completed_at") val completedAtMillis: Long?,
    @ColumnInfo(name = "position") val position: Int,
)
// ===== END SECTION: CHAPTER_ENTITY =====

// ===== SECTION: SUBJECT_ENTITY_MAPPING =====
fun SubjectWithCounts.toModel(): Subject = Subject(
    id = subject.id,
    name = subject.name,
    colorIndex = subject.colorIndex,
    chaptersDone = chaptersDone,
    chaptersTotal = chaptersTotal,
    createdAtMillis = subject.createdAtMillis,
)

fun SubjectEntity.toRef(): SubjectRef = SubjectRef(id = id, name = name, colorIndex = colorIndex)

fun ChapterEntity.toModel(): Chapter = Chapter(
    id = id,
    subjectId = subjectId,
    title = title,
    isDone = isDone,
    completedAtMillis = completedAtMillis,
    position = position,
)
// ===== END SECTION: SUBJECT_ENTITY_MAPPING =====
