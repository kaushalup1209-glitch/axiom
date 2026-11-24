package com.kaushalkr.axiom

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.testing.string
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// ===== SECTION: TEST_MAIN_ACTIVITY_LAUNCH =====
/** Launches the real activity: application container, DataStore, power monitor and first-frame gate. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class MainActivityLaunchTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_showsHomeAfterAppearanceLoads() {
        composeRule.waitUntilExactlyOneExists(hasText(string(R.string.home_hero_title)), timeoutMillis = 5_000)
        composeRule.onNodeWithText(string(R.string.home_title)).assertIsDisplayed()
    }
}
// ===== END SECTION: TEST_MAIN_ACTIVITY_LAUNCH =====
