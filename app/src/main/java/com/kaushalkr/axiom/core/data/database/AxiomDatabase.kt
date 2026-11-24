package com.kaushalkr.axiom.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaushalkr.axiom.core.data.focus.FocusSessionDao
import com.kaushalkr.axiom.core.data.focus.FocusSessionEntity

// ===== SECTION: AXIOM_DATABASE =====
/**
 * Local study database. Add entities for tasks (Phase 03), routines (Phase 04) and usage
 * records (Phase 06) here, with a schema version bump and a migration for each change.
 */
@Database(
    entities = [FocusSessionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AxiomDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        /** File name. Must match the backup rules in res/xml. */
        const val NAME = "axiom.db"
    }
}
// ===== END SECTION: AXIOM_DATABASE =====
