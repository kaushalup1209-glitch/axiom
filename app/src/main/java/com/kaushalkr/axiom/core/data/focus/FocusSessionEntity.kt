package com.kaushalkr.axiom.core.data.focus

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kaushalkr.axiom.core.model.FocusSession
import com.kaushalkr.axiom.core.model.FocusSessionSource

// ===== SECTION: FOCUS_SESSION_ENTITY =====
@Entity(
    tableName = "focus_sessions",
    indices = [Index(value = ["ended_at"])],
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAtMillis: Long,
    @ColumnInfo(name = "ended_at") val endedAtMillis: Long,
    @ColumnInfo(name = "planned_duration_ms") val plannedDurationMillis: Long,
    @ColumnInfo(name = "focused_duration_ms") val focusedDurationMillis: Long,
    @ColumnInfo(name = "completed") val completed: Boolean,
    @ColumnInfo(name = "source") val source: String,
)

fun FocusSessionEntity.toModel(): FocusSession = FocusSession(
    id = id,
    startedAtMillis = startedAtMillis,
    endedAtMillis = endedAtMillis,
    plannedDurationMillis = plannedDurationMillis,
    focusedDurationMillis = focusedDurationMillis,
    completed = completed,
    source = FocusSessionSource.entries.firstOrNull { it.name == source } ?: FocusSessionSource.QUICK_START,
)
// ===== END SECTION: FOCUS_SESSION_ENTITY =====
