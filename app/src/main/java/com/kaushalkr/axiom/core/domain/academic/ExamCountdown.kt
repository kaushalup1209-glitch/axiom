package com.kaushalkr.axiom.core.domain.academic

import com.kaushalkr.axiom.core.model.Exam
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ===== SECTION: EXAM_COUNTDOWN =====
/** Countdown rules for exams and deadlines. */
object ExamCountdown {

    /** Whole days from [today] to the exam; 0 on the day, negative once it has passed. */
    fun daysUntil(exam: Exam, today: LocalDate): Long = ChronoUnit.DAYS.between(today, exam.date)

    fun upcoming(exams: List<Exam>, today: LocalDate): List<Exam> = exams.filter { !it.date.isBefore(today) }

    fun past(exams: List<Exam>, today: LocalDate): List<Exam> = exams.filter { it.date.isBefore(today) }.reversed()

    fun next(exams: List<Exam>, today: LocalDate): Exam? = exams.firstOrNull { !it.date.isBefore(today) }
}
// ===== END SECTION: EXAM_COUNTDOWN =====
