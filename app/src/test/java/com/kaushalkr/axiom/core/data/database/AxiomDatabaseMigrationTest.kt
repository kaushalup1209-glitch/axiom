package com.kaushalkr.axiom.core.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.core.data.academic.SubjectRepository
import com.kaushalkr.axiom.core.data.routine.RoutineRepository
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.model.RoutineBlockDraft
import com.kaushalkr.axiom.core.model.TaskDraft
import com.kaushalkr.axiom.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// ===== SECTION: TEST_DATABASE_MIGRATION =====
/**
 * Builds real older databases from the exported schemas, stores data in them, then opens them
 * with the current [AxiomDatabase]. Room runs the auto-migrations and validates the resulting
 * schema; existing rows must survive and the newer tables must work.
 */
@RunWith(AndroidJUnit4::class)
class AxiomDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "migration-test.db"
    private val timeProvider = FakeTimeProvider(now = 1_795_514_400_000L)

    @After
    fun deleteDatabase() {
        context.deleteDatabase(name)
    }

    @Test
    fun version1DataSurvivesUpgradeToCurrentVersion() = runTest {
        createDatabase(version = 1) { db ->
            db.execSQL(
                "INSERT INTO focus_sessions (id, started_at, ended_at, planned_duration_ms, focused_duration_ms, completed, source) " +
                    "VALUES (7, 1000, 1501000, 1500000, 1500000, 1, 'quick')",
            )
        }

        val database = Room.databaseBuilder(context, AxiomDatabase::class.java, name).allowMainThreadQueries().build()
        try {
            val readable = database.openHelper.readableDatabase
            assertEquals(CURRENT_VERSION, readable.version)
            readable.query("SELECT focused_duration_ms FROM focus_sessions WHERE id = 7").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1_500_000L, cursor.getLong(0))
            }
            val repository = TaskRepository(database, timeProvider)
            val id = repository.save(0L, TaskDraft(title = "First task after upgrade"))
            assertEquals("First task after upgrade", repository.getTask(id)?.title)
        } finally {
            database.close()
        }
    }

    @Test
    fun version2TasksSurviveUpgradeAndRoutineTablesWork() = runTest {
        createDatabase(version = 2) { db ->
            db.execSQL(
                "INSERT INTO tasks (id, title, description, subject, priority, due_date, due_time, estimated_minutes, " +
                    "recurrence, completed_at, created_at, updated_at, sort_order) " +
                    "VALUES (3, 'Revise optics', '', 'Physics', 'HIGH', NULL, NULL, NULL, 'NONE', NULL, 1000, 1000, 1.0)",
            )
        }

        val database = Room.databaseBuilder(context, AxiomDatabase::class.java, name).allowMainThreadQueries().build()
        try {
            val readable = database.openHelper.readableDatabase
            assertEquals(CURRENT_VERSION, readable.version)
            readable.query("SELECT title, subject FROM tasks WHERE id = 3").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Revise optics", cursor.getString(0))
                assertEquals("Physics", cursor.getString(1))
            }
            val subjectId = SubjectRepository(database, timeProvider).create("Physics")
            assertNotNull(subjectId)
            val routines = RoutineRepository(database, timeProvider)
            val blockId = routines.save(
                0L,
                RoutineBlockDraft(
                    title = "Optics revision",
                    subjectId = subjectId,
                    startTime = "18:00",
                    durationMinutes = 60,
                    days = listOf(1, 3, 5),
                    linkedTaskIds = listOf(3L),
                ),
            )
            val block = routines.getBlock(blockId)
            assertEquals("Optics revision", block?.title)
            assertEquals(listOf(3L), block?.linkedTasks?.map { it.id })
        } finally {
            database.close()
        }
    }

    /** Creates a database at [version] from its exported schema, then runs [seed] on it. */
    private fun createDatabase(version: Int, seed: (SQLiteDatabase) -> Unit) {
        val schema = JSONObject(File(SCHEMA_DIR, "$version.json").readText()).getJSONObject("database")
        val file = context.getDatabasePath(name).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace(TABLE_PLACEHOLDER, table))
                val indices = entity.optJSONArray("indices") ?: continue
                for (i in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(i).getString("createSql").replace(TABLE_PLACEHOLDER, table))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) db.execSQL(setup.getString(index))
            seed(db)
            db.version = version
        }
    }

    private companion object {
        const val CURRENT_VERSION = 3
        const val SCHEMA_DIR = "schemas/com.kaushalkr.axiom.core.data.database.AxiomDatabase"
        const val TABLE_PLACEHOLDER = "\${TABLE_NAME}"
    }
}
// ===== END SECTION: TEST_DATABASE_MIGRATION =====
