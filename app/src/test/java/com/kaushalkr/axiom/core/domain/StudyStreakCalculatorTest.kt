package com.kaushalkr.axiom.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// ===== SECTION: TEST_STUDY_STREAK =====
class StudyStreakCalculatorTest {

    private val today = LocalDate.of(2026, 11, 24)
    private val minimum = 15 * 60_000L
    private val enough = minimum
    private val tooLittle = minimum - 1

    @Test
    fun countsConsecutiveDaysEndingToday() {
        val totals = mapOf(today to enough, today.minusDays(1) to enough, today.minusDays(2) to enough, today.minusDays(4) to enough)
        val streak = StudyStreakCalculator.calculate(totals, today, minimum)
        assertEquals(3, streak.currentDays)
        assertTrue(streak.todayMet)
    }

    @Test
    fun streakStaysAliveUntilTodayEnds() {
        val totals = mapOf(today.minusDays(1) to enough, today.minusDays(2) to enough, today to tooLittle)
        val streak = StudyStreakCalculator.calculate(totals, today, minimum)
        assertEquals(2, streak.currentDays)
        assertFalse(streak.todayMet)
    }

    @Test
    fun missedYesterdayResetsStreak() {
        val totals = mapOf(today.minusDays(2) to enough, today.minusDays(3) to enough)
        assertEquals(0, StudyStreakCalculator.calculate(totals, today, minimum).currentDays)
    }

    @Test
    fun lastSevenDaysEndToday() {
        val streak = StudyStreakCalculator.calculate(mapOf(today.minusDays(6) to enough), today, minimum)
        assertEquals(7, streak.lastSevenDays.size)
        assertEquals(today.minusDays(6), streak.lastSevenDays.first().date)
        assertTrue(streak.lastSevenDays.first().met)
        assertTrue(streak.lastSevenDays.last().isToday)
    }
}
// ===== END SECTION: TEST_STUDY_STREAK =====
