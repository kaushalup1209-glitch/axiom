package com.kaushalkr.axiom.core.model

// ===== SECTION: MODEL_FOCUS_SESSION =====
/** A finished focus session. Times are wall-clock epoch milliseconds. */
data class FocusSession(
    val id: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val plannedDurationMillis: Long,
    /** Time actually focused, excluding pauses. */
    val focusedDurationMillis: Long,
    /** True when the planned duration was reached. */
    val completed: Boolean,
    val source: FocusSessionSource,
)

enum class FocusSessionSource {
    QUICK_START,
}
// ===== END SECTION: MODEL_FOCUS_SESSION =====

// ===== SECTION: MODEL_ACTIVE_FOCUS_SESSION =====
/**
 * The session currently running or paused. Progress is derived from stored timestamps, never
 * from a ticking counter, so it stays exact while the app is in the background or killed.
 */
data class ActiveFocusSession(
    val startedAtMillis: Long,
    val plannedDurationMillis: Long,
    /** When the current pause began, or null while running. */
    val pausedAtMillis: Long?,
    /** Total length of all completed pauses. */
    val totalPausedMillis: Long,
) {
    val isPaused: Boolean get() = pausedAtMillis != null

    fun focusedMillis(nowMillis: Long): Long {
        val end = pausedAtMillis ?: nowMillis
        return (end - startedAtMillis - totalPausedMillis).coerceIn(0L, plannedDurationMillis)
    }

    fun remainingMillis(nowMillis: Long): Long = plannedDurationMillis - focusedMillis(nowMillis)

    fun progress(nowMillis: Long): Float =
        if (plannedDurationMillis <= 0L) 1f else focusedMillis(nowMillis).toFloat() / plannedDurationMillis

    /** The wall-clock moment the planned duration is reached if the session keeps running. */
    fun plannedEndMillis(): Long = startedAtMillis + totalPausedMillis + plannedDurationMillis
}
// ===== END SECTION: MODEL_ACTIVE_FOCUS_SESSION =====
