package com.aera.robot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.aera.robot.data.GeminiRepository
import com.aera.robot.data.LocalPreferences
import com.aera.robot.robot.RobotController
import com.aera.robot.robot.RobotState
import com.aera.robot.ui.screens.MainScreen
import com.aera.robot.ui.screens.SettingsScreen
import com.aera.robot.ui.theme.AeraRobotTheme
import com.aera.robot.voice.SpeechRecognizerManager
import com.aera.robot.voice.TextToSpeechManager

class MainActivity : ComponentActivity() {

    private lateinit var robotController: RobotController
    private lateinit var geminiRepo: GeminiRepository
    private lateinit var prefs: LocalPreferences
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var speechManager: SpeechRecognizerManager

    // Shared state for voice result
    private var voiceResultState = mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningProcess()
        } else {
            robotController.setState(RobotState.IDLE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        robotController = RobotController()
        geminiRepo = GeminiRepository()
        prefs = LocalPreferences(this)
        ttsManager = TextToSpeechManager(this)
        speechManager = SpeechRecognizerManager(this)

        // Load saved API key
        geminiRepo.setApiKey(prefs.apiKey)

        setContent {
            AeraRobotTheme {
                var currentScreen by remember { mutableStateOf("main") }
                val voiceResult by voiceResultState

                if (currentScreen == "main") {
                    MainScreen(
                        robotController = robotController,
                        geminiRepo = geminiRepo,
                        prefs = prefs,
                        ttsManager = ttsManager,
                        onOpenSettings = { currentScreen = "settings" },
                        onStartVoiceInput = {
                            checkAndRequestAudioPermission()
                        },
                        lastVoiceResult = voiceResult,
                        onVoiceResultConsumed = {
                            voiceResultState.value = null
                        }
                    )
                } else {
                    SettingsScreen(
                        prefs = prefs,
                        onApiKeyChanged = { key ->
                            geminiRepo.setApiKey(key)
                        },
                        onBack = { currentScreen = "main" }
                    )
                }
            }
        }
    }

    private fun checkAndRequestAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startListeningProcess()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startListeningProcess() {
        robotController.setState(RobotState.LISTENING)
        speechManager.startListening(
            onResult = { spokenText ->
                robotController.setState(RobotState.THINKING)
                voiceResultState.value = spokenText
            },
            onError = {
                robotController.setState(RobotState.IDLE)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
