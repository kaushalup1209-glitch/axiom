package com.kaushalkr.axiom.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.R
import com.kaushalkr.axiom.core.model.AnimationScale
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.navigation.TopLevelDestination
import com.kaushalkr.axiom.testing.AxiomTestApp
import com.kaushalkr.axiom.testing.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// ===== SECTION: TEST_APP_NAVIGATION =====
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AxiomAppNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val heroTitles = mapOf(
        TopLevelDestination.HOME to R.string.home_hero_title,
        TopLevelDestination.PLANNER to R.string.planner_hero_title,
        TopLevelDestination.FOCUS to R.string.focus_hero_title,
        TopLevelDestination.INSIGHTS to R.string.insights_hero_title,
        TopLevelDestination.ASSISTANT to R.string.assistant_hero_title,
    )

    private fun tab(destination: TopLevelDestination) =
        composeRule.onNode(hasText(string(destination.labelRes)) and isSelectable())

    private fun assertTabsNavigate() {
        composeRule.onNodeWithText(string(R.string.home_hero_title)).assertIsDisplayed()
        TopLevelDestination.entries.reversed().forEach { destination ->
            tab(destination).performClick()
            composeRule.waitForIdle()
            tab(destination).assertIsSelected()
            composeRule.onNodeWithText(string(heroTitles.getValue(destination))).assertIsDisplayed()
        }
    }

    @Test
    fun glassOff_allFiveTabsNavigate() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.OFF) }
        assertTabsNavigate()
    }

    @Test
    fun glassLite_allFiveTabsNavigate() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.LITE) }
        assertTabsNavigate()
    }

    @Test
    fun glassFull_allFiveTabsNavigate() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        assertTabsNavigate()
    }

    @Test
    fun animationScaleOff_allFiveTabsNavigate() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL, animationScale = AnimationScale.OFF) }
        assertTabsNavigate()
    }

    @Test
    fun animationScaleDouble_allFiveTabsNavigate() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL, animationScale = AnimationScale.DOUBLE) }
        assertTabsNavigate()
    }

    @Test
    fun settings_animationScaleSelection_isPersisted() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_animation_scale)), timeoutMillis = 5_000)

        val doubleOption = hasText(string(R.string.animation_scale_double)) and isSelectable()
        composeRule.onNode(doubleOption).performScrollTo().performClick()
        composeRule.waitUntilExactlyOneExists(doubleOption and isSelected(), timeoutMillis = 5_000)

        // Restore the default so other tests start from a clean preference.
        val normalOption = hasText(string(R.string.animation_scale_normal)) and isSelectable()
        composeRule.onNode(normalOption).performClick()
        composeRule.waitUntilExactlyOneExists(normalOption and isSelected(), timeoutMillis = 5_000)
    }

    @Test
    fun topActions_openShieldAndSettings_andBackReturns() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }

        composeRule.onNodeWithContentDescription(string(R.string.action_open_shield)).performClick()
        composeRule.onNodeWithText(string(R.string.shield_hero_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(string(R.string.action_navigate_back)).performClick()
        composeRule.onNodeWithText(string(R.string.home_hero_title)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_subtitle)), timeoutMillis = 5_000)
        composeRule.onNodeWithText(string(R.string.settings_glass)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(string(R.string.action_navigate_back)).performClick()
        composeRule.onNodeWithText(string(R.string.home_hero_title)).assertIsDisplayed()
    }

    @Test
    fun settings_resetDialog_opensAndCancels() {
        composeRule.setContent { AxiomTestApp(glassMode = GlassMode.FULL) }
        composeRule.onNodeWithContentDescription(string(R.string.action_open_settings)).performClick()
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.settings_reset_appearance)), timeoutMillis = 5_000)

        composeRule.onNodeWithText(string(R.string.settings_reset_appearance)).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.dialog_reset_title)).assertIsDisplayed()

        composeRule.onNodeWithText(string(R.string.dialog_cancel)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(string(R.string.dialog_reset_title)).assertDoesNotExist()
        composeRule.onNodeWithText(string(R.string.settings_reset_appearance)).assertIsDisplayed()
    }
}
// ===== END SECTION: TEST_APP_NAVIGATION =====
