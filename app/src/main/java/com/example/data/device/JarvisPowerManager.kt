package com.example.data.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class JarvisPowerMode(val label: String, val samplingDelayMs: Long, val isFullPower: Boolean) {
    TURBO_CHARGING("ARC REACTOR TURBO (100%)", 20L, true),
    BALANCED("BALANCED STARK MODE (80%)", 60L, false),
    ECO_SAVER("ECO STARK POWER (50%)", 160L, false)
}

class JarvisPowerManager(private val context: Context) {

    private val _currentPowerMode = MutableStateFlow(JarvisPowerMode.TURBO_CHARGING)
    val currentPowerMode: StateFlow<JarvisPowerMode> = _currentPowerMode

    fun evaluatePowerMode(): JarvisPowerMode {
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, batteryFilter)

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 100

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isSystemPowerSave = powerManager?.isPowerSaveMode == true

        val newMode = when {
            isCharging || batteryPct > 50 -> JarvisPowerMode.TURBO_CHARGING
            batteryPct in 21..50 && !isSystemPowerSave -> JarvisPowerMode.BALANCED
            else -> JarvisPowerMode.ECO_SAVER
        }

        _currentPowerMode.value = newMode
        return newMode
    }
}
