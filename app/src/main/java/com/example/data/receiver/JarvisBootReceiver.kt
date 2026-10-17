package com.example.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.service.JarvisForegroundService

class JarvisBootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "JarvisBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            try {
                JarvisForegroundService.startService(context)
            } catch (e: Exception) {
                Log.e(TAG, "Could not start JarvisForegroundService on boot: ${e.message}", e)
            }
        }
    }
}
