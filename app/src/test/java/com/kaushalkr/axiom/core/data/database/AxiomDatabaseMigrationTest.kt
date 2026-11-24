package com.kaushalkr.axiom.core.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaushalkr.axiom.core.data.task.TaskRepository
import com.kaushalkr.axiom.core.model.TaskDraft
import com.kaushalkr.axiom.testing.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// ===== SECTION: TEST_DATABASE_MIGRATION =====
/**
 * Builds a real version 1 database from the exported schema, stores a focus session, then
 * opens it with the current [AxiomDatabase]. Room runs the 1→2 migration and validates the
 * resulting schema; the session must survive and the new task tables must work.
 */
@RunWith(AndroidJUnit4::class)
class AxiomDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "migration-test.db"

    @After
    fun deleteDatabase() {
        context.deleteDatabase(name)
    }

    @Test
    fun version1DataSurvivesUpgradeToVersion2() = runTest {
        createVersion1Database()

        val database = Room.databaseBuilder(context, AxiomDatabase::class.java, name).allowMainThreadQueries().build()
        try {
            val readable = database.openHelper.readableDatabase
            assertEquals(2, readable.version)
            readable.query("SELECT focused_duration_ms FROM focus_sessions WHERE id = 7").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1_500_000L, cursor.getLong(0))
            }
            val repository = TaskRepository(database, FakeTimeProvider(now = 1_795_514_400_000L))
            val id = repository.save(0L, TaskDraft(title = "First task after upgrade"))
            assertEquals("First task after upgrade", repository.getTask(id)?.title)
        } finally {
            database.close()
        }
    }

    private fun createVersion1Database() {
        val schema = JSONObject(File(SCHEMA_DIR, "1.json").readText()).getJSONObject("database")
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
            db.execSQL(
                "INSERT INTO focus_sessions (id, started_at, ended_at, planned_duration_ms, focused_duration_ms, completed, source) " +
                    "VALUES (7, 1000, 1501000, 1500000, 1500000, 1, 'quick')",
            )
            db.version = 1
        }
    }

    private companion object {
        const val SCHEMA_DIR = "schemas/com.kaushalkr.axiom.core.data.database.AxiomDatabase"
        const val TABLE_PLACEHOLDER = "\${TABLE_NAME}"
    }
}
// ===== END SECTION: TEST_DATABASE_MIGRATION =====
