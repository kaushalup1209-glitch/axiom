package com.kaushalkr.axiom.core.data.focus

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// ===== SECTION: FOCUS_SESSION_DAO =====
@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    /** Sessions that ended at or after [sinceMillis], oldest first. */
    @Query("SELECT * FROM focus_sessions WHERE ended_at >= :sinceMillis ORDER BY ended_at ASC")
    fun observeEndedSince(sinceMillis: Long): Flow<List<FocusSessionEntity>>
}
// ===== END SECTION: FOCUS_SESSION_DAO =====
