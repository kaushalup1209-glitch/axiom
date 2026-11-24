package com.kaushalkr.axiom.feature.tasks

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.testing.AxiomTestApp
import com.kaushalkr.axiom.testing.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// ===== SECTION: TEST_TASKS_FLOW =====
/** End-to-end task flows through the real app, database and undo snackbar. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class TasksFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val timeout = 5_000L

    private fun waitFor(matcher: SemanticsMatcher) = composeRule.waitUntilAtLeastOneExists(matcher, timeoutMillis = timeout)

    private fun waitGone(matcher: SemanticsMatcher) = composeRule.waitUntilDoesNotExist(matcher, timeoutMillis = timeout)

    private fun format(id: Int, vararg args: Any): String =
        ApplicationProvider.getApplicationContext<Context>().getString(id, *args)

    private fun openPlanner() {
        waitFor(hasText(string(R.string.home_goal_title)))
        composeRule.onNode(hasText(string(R.string.nav_planner)) and isSelectable()).performClick()
        waitFor(hasText(string(R.string.tasks_title)))
    }

    private fun createTask(title: String) {
        composeRule.onNodeWithText(string(R.string.tasks_new)).performClick()
        waitFor(hasContentDescription(string(R.string.editor_title_label)))
        composeRule.onNodeWithContentDescription(string(R.string.editor_title_label)).performTextInput(title)
        composeRule.onNodeWithText(string(R.string.editor_save)).assertIsEnabled().performClick()
        // The editor closes and the new task's row (with its check) appears in Today.
        waitGone(hasContentDescription(string(R.string.editor_title_label)))
        waitFor(hasContentDescription(format(R.string.task_checkbox_complete, title)))
    }

    @Test
    fun createCompleteAndUndo() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE) }
        openPlanner()
        val title = "Read chapter 5"
        createTask(title)

        val check = hasContentDescription(format(R.string.task_checkbox_complete, title))
        composeRule.onNode(check).performClick()
        waitFor(hasText(format(R.string.undo_task_completed, title)))
        waitGone(hasText(title))

        composeRule.onNodeWithText(string(R.string.action_undo)).performClick()
        waitFor(hasText(title))
    }

    @Test
    fun swipeLeftDeletesAndUndoRestores() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        openPlanner()
        val title = "Lab report"
        createTask(title)

        composeRule.onNodeWithText(title).performTouchInput { swipeLeft() }
        waitFor(hasText(format(R.string.undo_task_deleted, title)))
        waitGone(hasText(title))

        composeRule.onNodeWithText(string(R.string.action_undo)).performClick()
        waitFor(hasText(title))
    }

    @Test
    fun editorAsksBeforeDiscardingChanges() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.OFF) }
        openPlanner()
        composeRule.onNodeWithText(string(R.string.tasks_new)).performClick()
        waitFor(hasContentDescription(string(R.string.editor_title_label)))
        composeRule.onNodeWithContentDescription(string(R.string.editor_title_label)).performTextInput("Unsaved idea")

        composeRule.onNodeWithContentDescription(string(R.string.action_navigate_back)).performClick()
        waitFor(hasText(string(R.string.dialog_discard_title)))
        composeRule.onNodeWithText(string(R.string.dialog_discard_keep)).performClick()
        waitGone(hasText(string(R.string.dialog_discard_title)))

        composeRule.onNodeWithContentDescription(string(R.string.action_navigate_back)).performClick()
        waitFor(hasText(string(R.string.dialog_discard_title)))
        composeRule.onNodeWithText(string(R.string.dialog_discard_confirm)).performClick()
        waitFor(hasText(string(R.string.tasks_search_placeholder)))
        composeRule.onNodeWithText("Unsaved idea").assertDoesNotExist()
    }

    @Test
    fun homeShowsTodaysTasksFromTheDatabase() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE) }
        openPlanner()
        val title = "Past paper section B"
        createTask(title)

        composeRule.onNode(hasText(string(R.string.nav_home)) and isSelectable()).performClick()
        waitFor(hasText(string(R.string.home_tasks_title)))
        composeRule.onNodeWithText(title).performScrollTo()
        composeRule.onNodeWithText(string(R.string.home_tasks_open_planner)).performScrollTo()
    }
}
// ===== END SECTION: TEST_TASKS_FLOW =====
