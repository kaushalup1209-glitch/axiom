package com.kaushalkr.axiom.core.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

// ===== SECTION: POWER_SAVE_MODE_MONITOR =====
/**
 * Emits the current Battery Saver state and every change to it.
 * The receiver is registered only while the flow is collected.
 */
class PowerSaveModeMonitor(context: Context) {

    private val appContext = context.applicationContext
    private val powerManager = appContext.getSystemService(PowerManager::class.java)

    val isPowerSaveMode: Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(powerManager?.isPowerSaveMode == true)
            }
        }
        trySend(powerManager?.isPowerSaveMode == true)
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        awaitClose { appContext.unregisterReceiver(receiver) }
    }
        .conflate()
        .distinctUntilChanged()
}
// ===== END SECTION: POWER_SAVE_MODE_MONITOR =====
