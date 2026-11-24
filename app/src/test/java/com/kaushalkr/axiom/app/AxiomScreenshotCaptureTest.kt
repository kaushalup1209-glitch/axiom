package com.kaushalkr.axiom.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.AxiomApplication
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.model.SubtaskDraft
import com.kaushalkr.axiom.core.model.TaskDraft
import com.kaushalkr.axiom.core.model.TaskPriority
import com.kaushalkr.axiom.core.model.TaskRecurrence
import com.kaushalkr.axiom.testing.AxiomTestApp
import com.kaushalkr.axiom.testing.string
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

// ===== SECTION: TEST_SCREENSHOT_CAPTURE =====
/**
 * Renders key screens with Robolectric native graphics and writes PNGs to
 * app/build/screenshots for manual layout review. It does not assert pixels.
 * Robolectric renders on the CPU, so blur and refraction shaders may not appear;
 * use a device or emulator to judge the real Liquid Glass effect.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-xxhdpi")
class AxiomScreenshotCaptureTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun waitForHome() {
        composeRule.waitUntilAtLeastOneExists(hasText(string(R.string.home_goal_title)), timeoutMillis = 5_000)
    }

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val directory = File("build/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    @Test
    fun home_light_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        waitForHome()
        capture("home_light_full")
    }

    @Test
    fun home_dark_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL, darkTheme = true) }
        waitForHome()
        capture("home_dark_full")
    }

    @Test
    fun home_light_off() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.OFF) }
        waitForHome()
        capture("home_light_off")
    }

    @Test
    fun settings_light_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_glass)), timeoutMillis = 5_000)
        capture("settings_light_full")
    }

    @Test
    fun settings_dark_off() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.OFF, darkTheme = true) }
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_glass)), timeoutMillis = 5_000)
        capture("settings_dark_off")
    }

    // ===== SECTION: TEST_SCREENSHOT_TASKS =====
    /** Test-only sample tasks written straight to the app database, so task screens have content. */
    private fun seedTasks() = runBlocking {
        val repository = ApplicationProvider.getApplicationContext<AxiomApplication>().container.taskRepository
        val today = repository.today()
        repository.save(
            0L,
            TaskDraft(
                title = "Finish chapter 4 notes",
                subject = "Biology",
                priority = TaskPriority.HIGH,
                dueDate = today.toString(),
                dueTime = "16:30",
                estimatedMinutes = 45,
                subtasks = listOf(SubtaskDraft("Summary", isDone = true), SubtaskDraft("Diagrams"), SubtaskDraft("Key terms")),
            ),
        )
        repository.save(0L, TaskDraft("Solve 20 integration problems", subject = "Mathematics", priority = TaskPriority.URGENT, dueDate = today.toString(), estimatedMinutes = 90))
        repository.save(0L, TaskDraft("Revise organic reactions", subject = "Chemistry", dueDate = today.toString(), recurrence = TaskRecurrence.WEEKDAYS))
        repository.save(0L, TaskDraft("Draft essay outline", subject = "English", priority = TaskPriority.LOW, dueDate = today.toString()))
        repository.save(0L, TaskDraft("Past paper, section B", subject = "Physics", priority = TaskPriority.HIGH, dueDate = today.minusDays(2).toString()))
        repository.save(0L, TaskDraft("Read history chapter", subject = "History", dueDate = today.plusDays(1).toString()))
    }

    private fun openPlanner() {
        waitForHome()
        composeRule.onNode(hasText(string(R.string.nav_planner)) and isSelectable()).performClick()
        composeRule.waitUntilAtLeastOneExists(hasText("Draft essay outline"), timeoutMillis = 5_000)
    }

    @Test
    fun tasks_light_full() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        openPlanner()
        capture("tasks_light_full")
    }

    @Test
    fun tasks_dark_lite() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE, darkTheme = true) }
        openPlanner()
        capture("tasks_dark_lite")
    }

    @Test
    @Config(qualifiers = "w393dp-h2300dp-xxhdpi")
    fun home_fullPage_tasks_light_full() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        waitForHome()
        composeRule.waitUntilAtLeastOneExists(hasText("Draft essay outline"), timeoutMillis = 5_000)
        capture("home_full_page_tasks_light_full")
    }

    @Test
    fun task_editor_light_full() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        openPlanner()
        composeRule.onNodeWithText("Finish chapter 4 notes").performClick()
        composeRule.waitUntilAtLeastOneExists(hasText(string(R.string.editor_title_edit)), timeoutMillis = 5_000)
        capture("task_editor_light_full")
    }

    @Test
    fun task_filter_sheet_dark_full() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL, darkTheme = true) }
        openPlanner()
        composeRule.onNodeWithContentDescription(string(R.string.tasks_filter)).performClick()
        composeRule.waitUntilAtLeastOneExists(hasText(string(R.string.filter_subject)), timeoutMillis = 5_000)
        capture("task_filter_sheet_dark_full")
    }

    @Test
    fun task_date_sheet_light_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        waitForHome()
        composeRule.onNode(hasText(string(R.string.nav_planner)) and isSelectable()).performClick()
        composeRule.waitUntilAtLeastOneExists(hasText(string(R.string.tasks_new)), timeoutMillis = 5_000)
        composeRule.onNodeWithText(string(R.string.tasks_new)).performClick()
        composeRule.waitUntilAtLeastOneExists(hasText(string(R.string.editor_date_pick)), timeoutMillis = 5_000)
        composeRule.onNodeWithText(string(R.string.editor_date_pick)).performScrollTo().performClick()
        composeRule.waitUntilAtLeastOneExists(hasText(string(R.string.editor_date_sheet_title)), timeoutMillis = 5_000)
        capture("task_date_sheet_light_full")
    }
    // ===== END SECTION: TEST_SCREENSHOT_TASKS =====

    // ===== SECTION: TEST_SCREENSHOT_LITE =====
    // Lite is the default for new installs: lightweight glass, blur only behind prominent surfaces.
    @Test
    fun home_light_lite() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE) }
        waitForHome()
        capture("home_light_lite")
    }

    @Test
    fun tasks_light_lite() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE) }
        openPlanner()
        capture("tasks_light_lite")
    }

    @Test
    fun task_filter_sheet_light_lite() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE) }
        openPlanner()
        composeRule.onNodeWithContentDescription(string(R.string.tasks_filter)).performClick()
        composeRule.waitUntilAtLeastOneExists(hasText(string(R.string.filter_subject)), timeoutMillis = 5_000)
        capture("task_filter_sheet_light_lite")
    }

    @Test
    fun settings_dark_lite() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE, darkTheme = true) }
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_glass)), timeoutMillis = 5_000)
        capture("settings_dark_lite")
    }

    @Test
    fun tasks_dark_off() {
        seedTasks()
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.OFF, darkTheme = true) }
        openPlanner()
        capture("tasks_dark_off")
    }
    // ===== END SECTION: TEST_SCREENSHOT_LITE =====

    @Test
    @Config(qualifiers = "w840dp-h1200dp-xhdpi")
    fun home_tablet_light_full() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        waitForHome()
        capture("home_tablet_light_full")
    }

    @Test
    @Config(qualifiers = "w393dp-h2300dp-xxhdpi")
    fun home_fullPage_dark_lite() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE, darkTheme = true) }
        waitForHome()
        capture("home_full_page_dark_lite")
    }
}
// ===== END SECTION: TEST_SCREENSHOT_CAPTURE =====
