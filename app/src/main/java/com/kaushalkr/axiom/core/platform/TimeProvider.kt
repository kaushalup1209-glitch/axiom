package com.kaushalkr.axiom.core.platform

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZoneId

// ===== SECTION: TIME_PROVIDER =====
/** Wall-clock time source; replaceable in tests. */
interface TimeProvider {
    fun nowMillis(): Long

    /** Current time zone, read each time so zone changes are picked up. */
    fun zone(): ZoneId

    /**
     * Emits the current time immediately, then at every multiple of [periodMillis]
     * (aligned to the wall clock, e.g. on the minute). Runs only while collected.
     */
    fun ticks(periodMillis: Long): Flow<Long>
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun zone(): ZoneId = ZoneId.systemDefault()

    override fun ticks(periodMillis: Long): Flow<Long> = flow {
        while (true) {
            val now = nowMillis()
            emit(now)
            delay(periodMillis - now % periodMillis)
        }
    }
}
// ===== END SECTION: TIME_PROVIDER =====
