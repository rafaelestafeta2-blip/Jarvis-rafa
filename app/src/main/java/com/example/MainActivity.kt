package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.data.db.JarvisDatabase
import com.example.data.device.DeviceController
import com.example.data.repository.JarvisRepository
import com.example.data.repository.RemindersRepository
import com.example.ui.navigation.JarvisAppNav
import com.example.ui.theme.JarvisTheme
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.JarvisViewModelFactory
import com.example.ui.viewmodel.RemindersViewModel
import com.example.ui.viewmodel.RemindersViewModelFactory
import com.example.ui.viewmodel.VoiceViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions result callback for audio/camera/phone state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request Record Audio, Camera, and Read Phone State permissions
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE
            )
        )

        val deviceController = DeviceController(applicationContext)

        val database = JarvisDatabase.getDatabase(applicationContext)
        val jarvisDao = database.jarvisDao()

        val jarvisRepository = JarvisRepository(jarvisDao, deviceController)
        val remindersRepository = RemindersRepository(jarvisDao)

        val jarvisViewModel: JarvisViewModel by viewModels {
            JarvisViewModelFactory(jarvisRepository)
        }

        val remindersViewModel: RemindersViewModel by viewModels {
            RemindersViewModelFactory(remindersRepository)
        }

        val voiceViewModel: VoiceViewModel by viewModels()

        setContent {
            JarvisTheme {
                JarvisAppNav(
                    jarvisViewModel = jarvisViewModel,
                    remindersViewModel = remindersViewModel,
                    voiceViewModel = voiceViewModel
                )
            }
        }
    }
}
