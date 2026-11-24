package com.kaushalkr.axiom.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaushalkr.axiom.core.data.academic.ChapterEntity
import com.kaushalkr.axiom.core.data.academic.ExamDao
import com.kaushalkr.axiom.core.data.academic.ExamEntity
import com.kaushalkr.axiom.core.data.academic.SubjectDao
import com.kaushalkr.axiom.core.data.academic.SubjectEntity
import com.kaushalkr.axiom.core.data.focus.FocusSessionDao
import com.kaushalkr.axiom.core.data.focus.FocusSessionEntity
import com.kaushalkr.axiom.core.data.routine.RoutineBlockEntity
import com.kaushalkr.axiom.core.data.routine.RoutineDao
import com.kaushalkr.axiom.core.data.routine.RoutineTaskLinkEntity
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
 * - v3 (Phase 04): subjects, chapters, exams, routine_blocks, routine_task_links
 *   (additive auto-migration; existing tables are untouched)
 */
@Database(
    entities = [
        FocusSessionEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        ExamEntity::class,
        RoutineBlockEntity::class,
        RoutineTaskLinkEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class AxiomDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao

    abstract fun taskDao(): TaskDao

    abstract fun subjectDao(): SubjectDao

    abstract fun examDao(): ExamDao

    abstract fun routineDao(): RoutineDao

    companion object {
        /** File name. Must match the backup rules in res/xml. */
        const val NAME = "axiom.db"
    }
}
// ===== END SECTION: AXIOM_DATABASE =====
