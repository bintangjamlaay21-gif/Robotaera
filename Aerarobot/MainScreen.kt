package com.aera.robot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aera.robot.data.GeminiRepository
import com.aera.robot.data.LocalPreferences
import com.aera.robot.robot.RobotController
import com.aera.robot.robot.RobotState
import com.aera.robot.ui.components.RobotFace
import com.aera.robot.ui.theme.DarkBackground
import com.aera.robot.ui.theme.NeonCyan
import com.aera.robot.ui.theme.SurfaceDark
import com.aera.robot.ui.theme.TextPrimary
import com.aera.robot.ui.theme.TextSecondary
import com.aera.robot.voice.TextToSpeechManager
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    robotController: RobotController,
    geminiRepo: GeminiRepository,
    prefs: LocalPreferences,
    ttsManager: TextToSpeechManager,
    onOpenSettings: () -> Unit,
    onStartVoiceInput: () -> Unit,
    lastVoiceResult: String?,
    onVoiceResultConsumed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            prefs.getConversationHistory().map { ChatMessage(it.first, it.second) }
        )
    }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Ketuk untuk membangunkan Aera") }

    // Wake up on first composition
    LaunchedEffect(Unit) {
        geminiRepo.setApiKey(prefs.apiKey)
        robotController.wakeUp {
            statusText = "Hai! Aku Aera. Ada yang bisa dibantu?"
        }
    }

    // Handle voice result from MainActivity
    LaunchedEffect(lastVoiceResult) {
        lastVoiceResult?.let { spoken ->
            onVoiceResultConsumed()
            if (spoken.isNotBlank() && !isProcessing) {
                sendMessage(
                    text = spoken,
                    messages = messages,
                    onMessagesUpdate = { messages = it },
                    robotController = robotController,
                    geminiRepo = geminiRepo,
                    prefs = prefs,
                    ttsManager = ttsManager,
                    scope = scope,
                    onProcessing = { isProcessing = it },
                    onStatus = { statusText = it }
                )
            }
        }
    }

    // Auto scroll
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Aera",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
        ) {
            // Robot Face Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RobotFace(state = robotController.robotState)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusText,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
            }

            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (!isProcessing) {
                            onStartVoiceInput()
                            statusText = "Mendengarkan..."
                            robotController.setState(RobotState.LISTENING)
                        }
                    },
                    enabled = !isProcessing
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice",
                        tint = if (robotController.robotState == RobotState.LISTENING) NeonCyan else TextPrimary
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Ketik pesan...", color = TextSecondary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isProcessing) {
                                val text = inputText.trim()
                                inputText = ""
                                keyboardController?.hide()
                                sendMessage(
                                    text = text,
                                    messages = messages,
                                    onMessagesUpdate = { messages = it },
                                    robotController = robotController,
                                    geminiRepo = geminiRepo,
                                    prefs = prefs,
                                    ttsManager = ttsManager,
                                    scope = scope,
                                    onProcessing = { isProcessing = it },
                                    onStatus = { statusText = it }
                                )
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isProcessing) {
                            val text = inputText.trim()
                            inputText = ""
                            keyboardController?.hide()
                            sendMessage(
                                text = text,
                                messages = messages,
                                onMessagesUpdate = { messages = it },
                                robotController = robotController,
                                geminiRepo = geminiRepo,
                                prefs = prefs,
                                ttsManager = ttsManager,
                                scope = scope,
                                onProcessing = { isProcessing = it },
                                onStatus = { statusText = it }
                            )
                        }
                    },
                    enabled = inputText.isNotBlank() && !isProcessing
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) NeonCyan else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) NeonCyan.copy(alpha = 0.2f) else SurfaceDark)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = TextPrimary,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

private fun sendMessage(
    text: String,
    messages: List<ChatMessage>,
    onMessagesUpdate: (List<ChatMessage>) -> Unit,
    robotController: RobotController,
    geminiRepo: GeminiRepository,
    prefs: LocalPreferences,
    ttsManager: TextToSpeechManager,
    scope: kotlinx.coroutines.CoroutineScope,
    onProcessing: (Boolean) -> Unit,
    onStatus: (String) -> Unit
) {
    onProcessing(true)
    robotController.setState(RobotState.THINKING)
    onStatus("Sedang berpikir...")

    val newMessages = messages + ChatMessage("user", text)
    onMessagesUpdate(newMessages)
    prefs.addToConversation("user", text)

    scope.launch {
        val history = prefs.getConversationHistory().dropLast(1) // exclude current
        val reply = geminiRepo.getResponse(text, history)

        val updated = newMessages + ChatMessage("model", reply)
        onMessagesUpdate(updated)
        prefs.addToConversation("model", reply)

        robotController.setState(RobotState.SPEAKING)
        onStatus("Aera berbicara...")

        if (prefs.autoSpeak) {
            ttsManager.speak(reply) {
                robotController.setState(RobotState.IDLE)
                onStatus("Siap mendengarkan")
                onProcessing(false)
            }
        } else {
            robotController.setState(RobotState.IDLE)
            onStatus("Siap mendengarkan")
            onProcessing(false)
        }
    }
}
