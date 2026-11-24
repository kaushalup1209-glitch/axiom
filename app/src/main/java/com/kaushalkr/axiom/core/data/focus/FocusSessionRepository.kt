package com.kaushalkr.axiom.core.data.focus

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.kaushalkr.axiom.core.model.ActiveFocusSession
import com.kaushalkr.axiom.core.model.FocusSessionSource
import com.kaushalkr.axiom.core.model.StudyDefaults
import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

/**
 * Focus session storage. The active session lives in DataStore as timestamps (it survives
 * process death and needs no background service); finished sessions are rows in Room.
 * All state changes are serialized by a mutex so a tick-triggered completion and a user
 * action can never record the same session twice.
 */
class FocusSessionRepository(
    private val dao: FocusSessionDao,
    private val stateStore: DataStore<Preferences>,
    private val timeProvider: TimeProvider,
) {
    private val mutex = Mutex()

    // ===== SECTION: FOCUS_REPOSITORY_READ =====
    val activeSession: Flow<ActiveFocusSession?> = stateStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toActiveSession() }
        .distinctUntilChanged()

    /** Focused milliseconds per local day (by session end), for days from [sinceDate] on. */
    fun dailyFocusTotals(sinceDate: LocalDate): Flow<Map<LocalDate, Long>> {
        val zone = timeProvider.zone()
        val sinceMillis = sinceDate.atStartOfDay(zone).toInstant().toEpochMilli()
        return dao.observeEndedSince(sinceMillis)
            .map { sessions ->
                sessions
                    .groupBy { Instant.ofEpochMilli(it.endedAtMillis).atZone(zone).toLocalDate() }
                    .mapValues { (_, daySessions) -> daySessions.sumOf { it.focusedDurationMillis } }
            }
            .distinctUntilChanged()
    }
    // ===== END SECTION: FOCUS_REPOSITORY_READ =====

    // ===== SECTION: FOCUS_REPOSITORY_CONTROL =====
    suspend fun start(plannedMinutes: Int) = mutex.withLock {
        if (currentSession() != null) return@withLock
        writeSession(
            ActiveFocusSession(
                startedAtMillis = timeProvider.nowMillis(),
                plannedDurationMillis = plannedMinutes * MILLIS_PER_MINUTE,
                pausedAtMillis = null,
                totalPausedMillis = 0L,
            ),
        )
    }

    suspend fun pause() = mutex.withLock {
        val session = currentSession() ?: return@withLock
        if (session.isPaused) return@withLock
        writeSession(session.copy(pausedAtMillis = timeProvider.nowMillis()))
    }

    suspend fun resume() = mutex.withLock {
        val session = currentSession() ?: return@withLock
        val pausedAt = session.pausedAtMillis ?: return@withLock
        val pausedFor = (timeProvider.nowMillis() - pausedAt).coerceAtLeast(0L)
        writeSession(session.copy(pausedAtMillis = null, totalPausedMillis = session.totalPausedMillis + pausedFor))
    }

    /** Ends the session now and saves the time focused so far (if it is long enough to count). */
    suspend fun finish() = mutex.withLock {
        val session = currentSession() ?: return@withLock
        val now = timeProvider.nowMillis()
        val focused = session.focusedMillis(now)
        clearSession()
        record(session, endedAtMillis = now, focusedMillis = focused, completed = focused >= session.plannedDurationMillis)
    }

    /** Saves and clears a running session whose planned time has passed (e.g. while backgrounded). */
    suspend fun completeIfDue() = mutex.withLock {
        val session = currentSession() ?: return@withLock
        if (session.isPaused || session.remainingMillis(timeProvider.nowMillis()) > 0L) return@withLock
        clearSession()
        record(
            session,
            endedAtMillis = session.plannedEndMillis(),
            focusedMillis = session.plannedDurationMillis,
            completed = true,
        )
    }
    // ===== END SECTION: FOCUS_REPOSITORY_CONTROL =====

    // ===== SECTION: FOCUS_REPOSITORY_STORAGE =====
    private suspend fun record(session: ActiveFocusSession, endedAtMillis: Long, focusedMillis: Long, completed: Boolean) {
        if (focusedMillis < StudyDefaults.MINIMUM_RECORDED_SESSION_MILLIS) return
        dao.insert(
            FocusSessionEntity(
                startedAtMillis = session.startedAtMillis,
                endedAtMillis = endedAtMillis,
                plannedDurationMillis = session.plannedDurationMillis,
                focusedDurationMillis = focusedMillis,
                completed = completed,
                source = FocusSessionSource.QUICK_START.name,
            ),
        )
    }

    private suspend fun currentSession(): ActiveFocusSession? =
        stateStore.data.catch { emit(emptyPreferences()) }.first().toActiveSession()

    private suspend fun writeSession(session: ActiveFocusSession) {
        stateStore.edit { preferences ->
            preferences[Keys.STARTED_AT] = session.startedAtMillis
            preferences[Keys.PLANNED_DURATION] = session.plannedDurationMillis
            preferences[Keys.TOTAL_PAUSED] = session.totalPausedMillis
            val pausedAt = session.pausedAtMillis
            if (pausedAt != null) preferences[Keys.PAUSED_AT] = pausedAt else preferences.remove(Keys.PAUSED_AT)
        }
    }

    private suspend fun clearSession() {
        stateStore.edit(MutablePreferences::clear)
    }

    private fun Preferences.toActiveSession(): ActiveFocusSession? {
        val startedAt = this[Keys.STARTED_AT] ?: return null
        val planned = this[Keys.PLANNED_DURATION] ?: return null
        return ActiveFocusSession(
            startedAtMillis = startedAt,
            plannedDurationMillis = planned,
            pausedAtMillis = this[Keys.PAUSED_AT],
            totalPausedMillis = this[Keys.TOTAL_PAUSED] ?: 0L,
        )
    }

    private object Keys {
        val STARTED_AT = longPreferencesKey("active_started_at")
        val PLANNED_DURATION = longPreferencesKey("active_planned_duration_ms")
        val PAUSED_AT = longPreferencesKey("active_paused_at")
        val TOTAL_PAUSED = longPreferencesKey("active_total_paused_ms")
    }
    // ===== END SECTION: FOCUS_REPOSITORY_STORAGE =====

    companion object {
        /** DataStore file for the active session. Not backed up: it is transient. */
        const val STATE_DATASTORE_NAME = "axiom_focus_state"
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
