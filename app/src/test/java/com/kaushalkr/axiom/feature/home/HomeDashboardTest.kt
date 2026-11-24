package com.kaushalkr.axiom.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.testing.AxiomTestApp
import com.kaushalkr.axiom.testing.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// ===== SECTION: TEST_HOME_DASHBOARD =====
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class HomeDashboardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun waitFor(text: String) =
        composeRule.waitUntilAtLeastOneExists(hasText(text), timeoutMillis = 5_000)

    @Test
    fun dashboardShowsEverySection() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        waitFor(string(R.string.home_goal_title))
        listOf(
            R.string.home_focus_title,
            R.string.home_stat_study_time,
            R.string.home_stat_distraction,
            R.string.home_streak_title,
            R.string.home_routine_title,
            R.string.home_tasks_title,
        ).forEach { id -> composeRule.onNodeWithText(string(id), useUnmergedTree = true).performScrollTo() }
    }

    @Test
    fun quickFocus_startPauseResumeFinish() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE) }
        waitFor(string(R.string.home_focus_start))

        composeRule.onNodeWithText(string(R.string.home_focus_start), useUnmergedTree = true).performScrollTo().performClick()
        waitFor(string(R.string.home_focus_pause))

        composeRule.onNodeWithText(string(R.string.home_focus_pause), useUnmergedTree = true).performClick()
        waitFor(string(R.string.home_focus_resume))

        composeRule.onNodeWithText(string(R.string.home_focus_resume), useUnmergedTree = true).performClick()
        waitFor(string(R.string.home_focus_pause))

        composeRule.onNodeWithText(string(R.string.home_focus_finish), useUnmergedTree = true).performClick()
        waitFor(string(R.string.home_focus_start))
    }
}
// ===== END SECTION: TEST_HOME_DASHBOARD =====
