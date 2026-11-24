package com.kaushalkr.axiom.testing

import com.kaushalkr.axiom.core.platform.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.ZoneId
import java.time.ZoneOffset

// ===== SECTION: TEST_FAKE_TIME_PROVIDER =====
/** Manually advanced clock in UTC. [ticks] emits the current time once. */
class FakeTimeProvider(var now: Long) : TimeProvider {
    override fun nowMillis(): Long = now

    override fun zone(): ZoneId = ZoneOffset.UTC

    override fun ticks(periodMillis: Long): Flow<Long> = flowOf(now)

    fun advanceMinutes(minutes: Long) {
        now += minutes * 60_000L
    }
}
// ===== END SECTION: TEST_FAKE_TIME_PROVIDER =====
