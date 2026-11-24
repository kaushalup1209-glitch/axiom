package com.kaushalkr.axiom.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

// ===== SECTION: MODEL_SUBJECT =====
/**
 * A subject with its syllabus progress. Tasks refer to subjects by name, so renaming a subject
 * also renames it on its tasks.
 */
@Immutable
data class Subject(
    val id: Long,
    val name: String,
    /** Index into the subject colour palette. */
    val colorIndex: Int,
    val chaptersDone: Int,
    val chaptersTotal: Int,
    val createdAtMillis: Long,
) {
    val progress: Float get() = if (chaptersTotal == 0) 0f else chaptersDone.toFloat() / chaptersTotal
    val ref: SubjectRef get() = SubjectRef(id = id, name = name, colorIndex = colorIndex)
}

/** One syllabus item (chapter, unit or topic) of a subject. */
@Immutable
data class Chapter(
    val id: Long,
    val subjectId: Long,
    val title: String,
    val isDone: Boolean,
    val completedAtMillis: Long?,
    val position: Int,
)

@Immutable
data class SubjectDetail(
    val subject: Subject,
    val chapters: List<Chapter>,
)
// ===== END SECTION: MODEL_SUBJECT =====

// ===== SECTION: MODEL_EXAM =====
/** Exams and other academic deadlines with a countdown. */
@Serializable
enum class ExamKind {
    EXAM,
    TEST,
    ASSIGNMENT,
    PROJECT,
    DEADLINE,
}

@Immutable
data class Exam(
    val id: Long,
    val title: String,
    val kind: ExamKind,
    val subject: SubjectRef?,
    val date: LocalDate,
    val time: LocalTime?,
    val notes: String,
    val createdAtMillis: Long,
)

/** Everything needed to create or replace an exam; serializable for the AI assistant. */
@Immutable
@Serializable
data class ExamDraft(
    val title: String,
    val kind: ExamKind = ExamKind.EXAM,
    val subjectId: Long? = null,
    /** ISO-8601 date (yyyy-MM-dd). */
    val date: String,
    /** Local time HH:mm, or null. */
    val time: String? = null,
    val notes: String = "",
) {
    val parsedDate: LocalDate get() = LocalDate.parse(date)
    val parsedTime: LocalTime? get() = time?.let(LocalTime::parse)
}
// ===== END SECTION: MODEL_EXAM =====

// ===== SECTION: MODEL_ACADEMIC_LIMITS =====
object AcademicLimits {
    const val SUBJECT_NAME_MAX_LENGTH = 40
    const val CHAPTER_TITLE_MAX_LENGTH = 120
    const val MAX_CHAPTERS_PER_SUBJECT = 200
    const val EXAM_TITLE_MAX_LENGTH = 100
    const val EXAM_NOTES_MAX_LENGTH = 1_000

    /** Number of colours in the subject palette. */
    const val SUBJECT_COLOR_COUNT = 8
}
// ===== END SECTION: MODEL_ACADEMIC_LIMITS =====
