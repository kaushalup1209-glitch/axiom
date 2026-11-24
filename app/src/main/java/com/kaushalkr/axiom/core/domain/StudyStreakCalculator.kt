package com.kaushalkr.axiom.core.domain

import androidx.compose.runtime.Immutable
import java.time.LocalDate

// ===== SECTION: DOMAIN_STUDY_STREAK_MODEL =====
@Immutable
data class StudyDay(
    val date: LocalDate,
    val met: Boolean,
    val isToday: Boolean,
)

@Immutable
data class StudyStreak(
    /** Consecutive qualifying days ending today, or yesterday if today has not qualified yet. */
    val currentDays: Int,
    val todayMet: Boolean,
    /** The last seven days, oldest first, ending today. */
    val lastSevenDays: List<StudyDay>,
)
// ===== END SECTION: DOMAIN_STUDY_STREAK_MODEL =====

// ===== SECTION: DOMAIN_STUDY_STREAK_CALCULATOR =====
/** Pure streak rules, kept free of Android so they are unit tested directly. */
object StudyStreakCalculator {

    fun calculate(dailyFocusMillis: Map<LocalDate, Long>, today: LocalDate, minimumMillis: Long): StudyStreak {
        fun met(date: LocalDate) = (dailyFocusMillis[date] ?: 0L) >= minimumMillis

        val todayMet = met(today)
        var day = if (todayMet) today else today.minusDays(1)
        var count = 0
        while (met(day)) {
            count++
            day = day.minusDays(1)
        }
        val week = (6L downTo 0L).map { offset ->
            val date = today.minusDays(offset)
            StudyDay(date = date, met = met(date), isToday = offset == 0L)
        }
        return StudyStreak(currentDays = count, todayMet = todayMet, lastSevenDays = week)
    }
}
// ===== END SECTION: DOMAIN_STUDY_STREAK_CALCULATOR =====
