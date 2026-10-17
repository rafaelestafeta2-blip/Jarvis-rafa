package com.example.data.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.io.File

class DeviceController(private val context: Context) {

    private var isFlashlightOn = false

    fun getBatteryPercentage(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            100
        }
    }

    fun isBatteryCharging(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun getStorageInfo(): Pair<Long, Long> {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val totalBlocks = stat.blockCountLong
        return Pair(availableBlocks * blockSize, totalBlocks * blockSize)
    }

    fun getFormattedFreeStorage(): String {
        val (freeBytes, totalBytes) = getStorageInfo()
        val freeGb = freeBytes / (1024 * 1024 * 1024.0)
        val totalGb = totalBytes / (1024 * 1024 * 1024.0)
        return "%.1f GB livres de %.1f GB".format(freeGb, totalGb)
    }

    fun getNetworkStatus(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "OFFLINE"
        val capabilities = cm.getNetworkCapabilities(network) ?: return "DESCONECTADO"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WI-FI CONECTADO"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "REDE MÓVEL DADOS ATIVA"
            else -> "CONECTADO"
        }
    }

    fun toggleFlashlight(enable: Boolean): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return "Nenhuma câmera encontrada para lanterna."
            cameraManager.setTorchMode(cameraId, enable)
            isFlashlightOn = enable
            if (enable) "Lanterna ativada, Sir." else "Lanterna desligada."
        } catch (e: Exception) {
            "Erro ao alternar lanterna: ${e.message}"
        }
    }

    fun isFlashlightActive(): Boolean = isFlashlightOn

    fun setVolumePercentage(percent: Int): String {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVol = (maxVol * (percent.coerceIn(0, 100) / 100.0)).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
            "Volume da mídia definido para $percent%, Sir."
        } catch (e: Exception) {
            "Falha ao ajustar volume: ${e.message}"
        }
    }

    fun openApplication(appNameQuery: String): String {
        val query = appNameQuery.lowercase().trim()
        val pm = context.packageManager

        // Known direct system intents
        when {
            query.contains("câmera") || query.contains("camera") -> {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return "Abrindo a câmera, Sir."
            }
            query.contains("configuração") || query.contains("configurações") || query.contains("ajustes") -> {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return "Abrindo as configurações do sistema, Sir."
            }
            query.contains("galeria") || query.contains("fotos") -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return "Abrindo a galeria de imagens, Sir."
            }
            query.contains("telefone") || query.contains("ligar") || query.contains("discador") -> {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return "Abrindo o discador de telefone, Sir."
            }
        }

        // Search installed apps matching query label or package name
        try {
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in packages) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                val pkgName = app.packageName.lowercase()

                if (label.contains(query) || pkgName.contains(query)) {
                    val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        val appLabel = pm.getApplicationLabel(app)
                        return "Iniciando o aplicativo $appLabel, Sir."
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore package scan failures
        }

        return "Sir, não encontrei um aplicativo instalado correspondente a '$appNameQuery'."
    }

    fun generateFullDiagnosticReport(): String {
        val batteryPct = getBatteryPercentage()
        val charging = if (isBatteryCharging()) "CARREGANDO" else "EM BATERIA"
        val storageStr = getFormattedFreeStorage()
        val netStr = getNetworkStatus()
        val androidVer = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        return """
DIAGNÓSTICO COMPLETO DOS SISTEMAS JARVIS:
• CÉLULA DE ENERGIA: $batteryPct% ($charging)
• ARMAZENAMENTO INTERNO: $storageStr
• CANAL DE REDE: $netStr
• KERNEL DO DISPOSITIVO: $androidVer
• NÚCLEO DE IA: Google Gemini 3.5-Flash Ativo
• AUTO-INICIALIZAÇÃO NO BOOT: Habilitada
• ESTADO DOS REATORES: 100% Estáveis
        """.trimIndent()
    }
}
