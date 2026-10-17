package com.example.data.device

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.provider.Settings

/**
 * Utilitário especializado na identificação e inicialização de aplicativos instalados no sistema Android.
 * Suporta apelidos em português (ex: "zap", "whassap", "whats", "insta", "youtube", "câmera").
 */
class AppLauncherHelper(private val context: Context) {

    fun launchApp(rawQuery: String): String {
        val query = rawQuery.lowercase().trim()
        val pm = context.packageManager

        // 1. Atalhos para intenções diretas do sistema Android
        if (query.contains("câmera") || query.contains("camera") || query.contains("foto")) {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                "Abrindo a câmera, Sir."
            } catch (e: Exception) {
                "Não foi possível abrir a câmera diretamente."
            }
        }

        if (query.contains("configuração") || query.contains("configurações") || query.contains("ajustes")) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                "Abrindo as configurações do sistema, Sir."
            } catch (e: Exception) {
                "Falha ao abrir as configurações."
            }
        }

        // 2. Mapeamento direto de pacotes conhecidos com apelidos populares em português
        val knownPackages = mapOf(
            listOf("whatsapp", "whassap", "watsapp", "zap", "whats", "wats", "wasap", "whatsap") to listOf("com.whatsapp", "com.whatsapp.w4b"),
            listOf("instagram", "insta") to listOf("com.instagram.android"),
            listOf("youtube", "tube") to listOf("com.google.android.youtube"),
            listOf("spotify", "música", "musica") to listOf("com.spotify.music"),
            listOf("chrome", "navegador", "internet", "browser") to listOf("com.android.chrome"),
            listOf("gmail", "email", "e-mail") to listOf("com.google.android.gm"),
            listOf("maps", "mapa", "gps", "waze") to listOf("com.google.android.apps.maps", "com.waze"),
            listOf("telegram") to listOf("org.telegram.messenger"),
            listOf("tiktok") to listOf("com.zhiliaoapp.musically")
        )

        for ((aliases, packageList) in knownPackages) {
            if (aliases.any { query.contains(it) }) {
                for (pkg in packageList) {
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        return try {
                            context.startActivity(launchIntent)
                            val appInfo = pm.getApplicationInfo(pkg, 0)
                            val label = pm.getApplicationLabel(appInfo)
                            "Iniciando o aplicativo $label, Sir."
                        } catch (e: Exception) {
                            "Sir, encontrei o aplicativo, mas o sistema bloqueou a abertura."
                        }
                    }
                }
            }
        }

        // 3. Varredura geral de aplicativos instalados no dispositivo
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
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
        } catch (_: Exception) {}

        return "Sir, o aplicativo '$rawQuery' não está instalado ou não foi localizado no dispositivo."
    }
}
