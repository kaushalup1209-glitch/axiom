package com.kaushalkr.axiom.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaushalkr.axiom.core.data.focus.FocusSessionDao
import com.kaushalkr.axiom.core.data.focus.FocusSessionEntity
import com.kaushalkr.axiom.core.data.task.SubtaskEntity
import com.kaushalkr.axiom.core.data.task.TaskDao
import com.kaushalkr.axiom.core.data.task.TaskEntity

// ===== SECTION: AXIOM_DATABASE =====
/**
 * Local study database. Every schema change bumps [version] and adds a migration; destructive
 * fallback is never enabled, so user data always survives an update.
 *
 * - v1 (Phase 02): focus_sessions
 * - v2 (Phase 03): tasks, subtasks (additive auto-migration)
 */
@Database(
    entities = [FocusSessionEntity::class, TaskEntity::class, SubtaskEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class AxiomDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao

    abstract fun taskDao(): TaskDao

    companion object {
        /** File name. Must match the backup rules in res/xml. */
        const val NAME = "axiom.db"
    }
}
// ===== END SECTION: AXIOM_DATABASE =====
