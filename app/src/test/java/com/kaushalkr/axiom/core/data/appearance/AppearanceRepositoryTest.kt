package com.kaushalkr.axiom.core.data.appearance

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kaushalkr.axiom.core.model.AnimationScale
import com.kaushalkr.axiom.core.model.AppearanceSettings
import com.kaushalkr.axiom.core.model.GlassMode
import com.kaushalkr.axiom.core.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// ===== SECTION: TEST_APPEARANCE_REPOSITORY =====
@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceRepositoryTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private fun TestScope.createRepository(): AppearanceRepository {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { temporaryFolder.newFile("appearance_test.preferences_pb") },
        )
        return AppearanceRepository(dataStore)
    }

    @Test
    fun emptyStore_emitsDefaults() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()
        assertEquals(AppearanceSettings(), repository.settings.first())
    }

    @Test
    fun writes_arePersistedAndRead() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()
        repository.setThemeMode(ThemeMode.DARK)
        repository.setGlassMode(GlassMode.LITE)
        repository.setGlassTintStrength(0.8f)
        repository.setAdaptGlassToBatterySaver(false)
        repository.setAnimationScale(AnimationScale.ONE_AND_HALF)

        assertEquals(
            AppearanceSettings(
                themeMode = ThemeMode.DARK,
                glassMode = GlassMode.LITE,
                glassTintStrength = 0.8f,
                adaptGlassToBatterySaver = false,
                animationScale = AnimationScale.ONE_AND_HALF,
            ),
            repository.settings.first(),
        )
    }

    @Test
    fun tintStrength_isClampedToUnitRange() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()
        repository.setGlassTintStrength(4f)
        assertEquals(1f, repository.settings.first().glassTintStrength)
        repository.setGlassTintStrength(-1f)
        assertEquals(0f, repository.settings.first().glassTintStrength)
    }

    @Test
    fun resetAppearance_restoresDefaults() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()
        repository.setThemeMode(ThemeMode.LIGHT)
        repository.setGlassMode(GlassMode.OFF)
        repository.setAnimationScale(AnimationScale.OFF)
        repository.resetAppearance()
        assertEquals(AppearanceSettings(), repository.settings.first())
    }
}
// ===== END SECTION: TEST_APPEARANCE_REPOSITORY =====
