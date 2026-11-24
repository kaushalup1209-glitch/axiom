package com.kaushalkr.axiom.core.data.study

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kaushalkr.axiom.core.model.StudyDefaults
import com.kaushalkr.axiom.core.model.StudyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Persists the user's name, daily study goal and last quick-start length. */
class StudyPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {

    // ===== SECTION: STUDY_PREFERENCES_READ =====
    val preferences: Flow<StudyPreferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences ->
            StudyPreferences(
                userName = preferences[Keys.USER_NAME].orEmpty(),
                dailyGoalMinutes = (preferences[Keys.DAILY_GOAL_MINUTES] ?: StudyDefaults.DAILY_GOAL_MINUTES)
                    .coerceIn(StudyDefaults.DAILY_GOAL_MIN_MINUTES, StudyDefaults.DAILY_GOAL_MAX_MINUTES),
                quickStartMinutes = preferences[Keys.QUICK_START_MINUTES]
                    ?.takeIf { it in StudyDefaults.QUICK_START_PRESETS_MINUTES }
                    ?: StudyDefaults.QUICK_START_MINUTES,
            )
        }
        .distinctUntilChanged()
    // ===== END SECTION: STUDY_PREFERENCES_READ =====

    // ===== SECTION: STUDY_PREFERENCES_WRITE =====
    suspend fun setUserName(name: String) {
        val cleaned = name.trim().take(StudyDefaults.USER_NAME_MAX_LENGTH)
        dataStore.edit { preferences ->
            if (cleaned.isEmpty()) preferences.remove(Keys.USER_NAME) else preferences[Keys.USER_NAME] = cleaned
        }
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        dataStore.edit {
            it[Keys.DAILY_GOAL_MINUTES] = minutes.coerceIn(
                StudyDefaults.DAILY_GOAL_MIN_MINUTES,
                StudyDefaults.DAILY_GOAL_MAX_MINUTES,
            )
        }
    }

    suspend fun setQuickStartMinutes(minutes: Int) {
        if (minutes !in StudyDefaults.QUICK_START_PRESETS_MINUTES) return
        dataStore.edit { it[Keys.QUICK_START_MINUTES] = minutes }
    }
    // ===== END SECTION: STUDY_PREFERENCES_WRITE =====

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val QUICK_START_MINUTES = intPreferencesKey("quick_start_minutes")
    }

    companion object {
        /** DataStore file name. Must match the backup rules in res/xml. */
        const val DATASTORE_NAME = "axiom_study"
    }
}
