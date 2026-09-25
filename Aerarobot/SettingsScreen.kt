package com.aera.robot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aera.robot.data.LocalPreferences
import com.aera.robot.ui.theme.DarkBackground
import com.aera.robot.ui.theme.NeonCyan
import com.aera.robot.ui.theme.SurfaceDark
import com.aera.robot.ui.theme.TextPrimary
import com.aera.robot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: LocalPreferences,
    onApiKeyChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    var autoSpeak by remember { mutableStateOf(prefs.autoSpeak) }
    var wakeWord by remember { mutableStateOf(prefs.wakeWordEnabled) }
    var memory by remember { mutableStateOf(prefs.memoryEnabled) }
    var apiKey by remember { mutableStateOf(prefs.apiKey) }
    var showSaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings Aera",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Key Section
            Text(
                "Gemini API Key",
                color = NeonCyan,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan API Key Gemini...", color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Button(
                onClick = {
                    prefs.apiKey = apiKey.trim()
                    onApiKeyChanged(apiKey.trim())
                    showSaved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan API Key", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
            if (showSaved) {
                Text(
                    "✓ API Key tersimpan",
                    color = NeonCyan,
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(vertical = 8.dp))

            // Toggles
            SettingSwitch(
                title = "Auto Speak (Suara Aera)",
                subtitle = "Aera akan berbicara dengan suara saat membalas",
                checked = autoSpeak,
                onCheckedChange = {
                    autoSpeak = it
                    prefs.autoSpeak = it
                }
            )

            SettingSwitch(
                title = "Wake Word (\"Aera\")",
                subtitle = "Fitur ini masih dalam pengembangan",
                checked = wakeWord,
                onCheckedChange = {
                    wakeWord = it
                    prefs.wakeWordEnabled = it
                }
            )

            SettingSwitch(
                title = "Memory Percakapan",
                subtitle = "Simpan riwayat chat agar Aera ingat konteks",
                checked = memory,
                onCheckedChange = {
                    memory = it
                    prefs.memoryEnabled = it
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { prefs.clearMemory() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear Memory & Percakapan")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Cara mendapatkan API Key:\n1. Buka aistudio.google.com\n2. Buat API Key baru\n3. Paste di atas lalu Simpan",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceDark
            )
        )
    }
}
