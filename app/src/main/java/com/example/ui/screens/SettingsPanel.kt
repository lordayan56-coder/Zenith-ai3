package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SettingsPanel() {
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
    var wakeWord by remember { mutableStateOf("Zenith") }
    var maleVoicePitch by remember { mutableStateOf(0.85f) }
    var hologramIntensity by remember { mutableStateOf(0.9f) }
    var autoMemoryIndex by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = ZenithCyanPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "ZENITH SYSTEM SETTINGS CENTER",
                color = ZenithCyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // 1. AI Core Model Selection
        SettingCard(title = "AI ENGINE MODEL") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("gemini-3.5-flash", "gemini-3.1-pro-preview").forEach { model ->
                    FilterChip(
                        selected = selectedModel == model,
                        onClick = { selectedModel = model },
                        label = { Text(model, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ZenithCyanPrimary,
                            selectedLabelColor = ZenithBackgroundDark,
                            containerColor = ZenithSurfaceVariant,
                            labelColor = ZenithTextSecondary
                        )
                    )
                }
            }
        }

        // 2. Adult Male Voice Modulation
        SettingCard(title = "MALE VOICE PITCH (0.85 CINEMATIC)") {
            Column {
                Slider(
                    value = maleVoicePitch,
                    onValueChange = { maleVoicePitch = it },
                    valueRange = 0.6f..1.2f,
                    colors = SliderDefaults.colors(
                        thumbColor = ZenithCyanPrimary,
                        activeTrackColor = ZenithCyanPrimary
                    )
                )
                Text(
                    text = "Pitch: ${String.format("%.2f", maleVoicePitch)} (Deep Natural Male Persona)",
                    color = ZenithTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 3. Hologram Particle Intensity
        SettingCard(title = "HOLOGRAM CORE INTENSITY") {
            Slider(
                value = hologramIntensity,
                onValueChange = { hologramIntensity = it },
                valueRange = 0.3f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = ZenithVioletSecondary,
                    activeTrackColor = ZenithVioletSecondary
                )
            )
        }

        // 4. Memory Behavior
        SettingCard(title = "MEMORY AUTOMATION") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto-Index User Conversations into Room Memory",
                    color = ZenithTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = autoMemoryIndex,
                    onCheckedChange = { autoMemoryIndex = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ZenithCyanPrimary,
                        checkedTrackColor = ZenithSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = ZenithSurfaceDark,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZenithSurfaceBorder, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                color = ZenithCyanPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}
