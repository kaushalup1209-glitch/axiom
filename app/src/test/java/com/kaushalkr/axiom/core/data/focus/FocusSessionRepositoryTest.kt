package com.kaushalkr.axiom.core.data.focus

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.core.data.database.AxiomDatabase
import com.kaushalkr.axiom.testing.FakeTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.time.LocalDate

// ===== SECTION: TEST_FOCUS_SESSION_REPOSITORY =====
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FocusSessionRepositoryTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        AxiomDatabase::class.java,
    ).allowMainThreadQueries().build()

    // 2026-11-24 10:00 UTC
    private val time = FakeTimeProvider(now = 1_795_514_400_000L)

    @After
    fun closeDatabase() = database.close()

    private fun TestScope.repository() = FocusSessionRepository(
        dao = database.focusSessionDao(),
        stateStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            temporaryFolder.newFile("focus_state.preferences_pb")
        },
        timeProvider = time,
    )

    private val today: LocalDate get() = LocalDate.of(2026, 11, 24)

    @Test
    fun pausedTimeIsExcludedFromRecordedFocus() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        repository.start(plannedMinutes = 25)
        time.advanceMinutes(10)
        repository.pause()
        time.advanceMinutes(30)
        repository.resume()
        time.advanceMinutes(5)
        repository.finish()

        assertNull(repository.activeSession.first())
        val totals = repository.dailyFocusTotals(today.minusDays(1)).first()
        assertEquals(15 * 60_000L, totals[today])
    }

    @Test
    fun dueSessionCompletesAtPlannedLength() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        repository.start(plannedMinutes = 25)
        time.advanceMinutes(90)
        repository.completeIfDue()

        assertNull(repository.activeSession.first())
        assertEquals(25 * 60_000L, repository.dailyFocusTotals(today.minusDays(1)).first()[today])
    }

    @Test
    fun sessionsUnderAMinuteAreDiscarded() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        repository.start(plannedMinutes = 25)
        time.now += 30_000L
        repository.finish()
        assertTrue(repository.dailyFocusTotals(today.minusDays(1)).first().isEmpty())
    }

    @Test
    fun runningSessionIsNotCompletedEarly() = runTest(UnconfinedTestDispatcher()) {
        val repository = repository()
        repository.start(plannedMinutes = 25)
        time.advanceMinutes(10)
        repository.completeIfDue()
        val active = repository.activeSession.first()
        assertEquals(15 * 60_000L, active?.remainingMillis(time.now))
    }
}
// ===== END SECTION: TEST_FOCUS_SESSION_REPOSITORY =====
