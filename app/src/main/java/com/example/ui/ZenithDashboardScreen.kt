package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hologram.HologramCoreState
import com.example.hologram.HologramCoreView
import com.example.ui.components.OwnerApprovalDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenithDashboardScreen(viewModel: ZenithViewModel) {
    val hologramState by viewModel.hologramState.collectAsState()
    val activeIntent by viewModel.activeIntent.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val activePanel by viewModel.activePanel.collectAsState()

    val memories by viewModel.allMemories.collectAsState()
    val permissions by viewModel.permissionStates.collectAsState()
    val telemetry by viewModel.telemetryState.collectAsState()

    val healthList by viewModel.diagnosticsManager.diagnosticResults.collectAsState()
    val isRunningDiagnostics by viewModel.diagnosticsManager.isRunningDiagnostics.collectAsState()

    val currentVersion by viewModel.updateManager.currentVersion.collectAsState()
    val stagedUpdate by viewModel.updateManager.availableUpdate.collectAsState()
    val updateLogs by viewModel.updateManager.updateLog.collectAsState()

    val plugins by viewModel.pluginManager.plugins.collectAsState()
    val activeApprovalRequest by viewModel.approvalManager.activeRequest.collectAsState()

    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val audioAmplitude by viewModel.voiceEngine.audioAmplitude.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val quickPills = listOf(
        "Open WhatsApp",
        "Remind me to message Ali tomorrow",
        "Show system status",
        "Check permissions",
        "Run diagnostics",
        "Check for Zenith update"
    )

    Scaffold(
        containerColor = ZenithBackgroundDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZenithSurfaceDark)
                    .border(1.dp, ZenithSurfaceBorder)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ZenithCyanPrimary)
                        )
                        Text(
                            text = "ZENITH AI",
                            color = ZenithCyanPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }

                    Surface(
                        color = ZenithSurfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (hologramState) {
                                            HologramCoreState.IDLE -> ZenithCyanPrimary
                                            HologramCoreState.LISTENING -> ZenithGoldAction
                                            HologramCoreState.THINKING -> ZenithVioletSecondary
                                            HologramCoreState.WARNING -> ZenithRedAlert
                                            else -> ZenithGreenSuccess
                                        }
                                    )
                            )
                            Text(
                                text = hologramState.label,
                                color = ZenithTextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NavigationPanel.values().forEach { panel ->
                        FilterChip(
                            selected = activePanel == panel,
                            onClick = { viewModel.activePanel.value = panel },
                            label = {
                                Text(
                                    text = panel.name,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZenithCyanPrimary,
                                selectedLabelColor = ZenithBackgroundDark,
                                containerColor = ZenithSurfaceDark,
                                labelColor = ZenithTextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp)
            ) {
                // Center Volumetric Hologram Core View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    HologramCoreView(
                        state = hologramState,
                        audioAmplitude = audioAmplitude,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Panel Drawer View (HUD, Memory, Permissions, Diagnostics, Updates, Plugins, Settings)
                Surface(
                    color = ZenithSurfaceDark.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZenithSurfaceBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    when (activePanel) {
                        NavigationPanel.HUD -> HudPanel(intent = activeIntent, telemetry = telemetry)
                        NavigationPanel.MEMORY -> MemoryPanel(
                            memories = memories,
                            onDeleteMemory = { viewModel.deleteMemory(it) },
                            onClearAllMemory = { viewModel.clearAllMemory() }
                        )
                        NavigationPanel.PERMISSIONS -> PermissionsPanel(
                            permissions = permissions,
                            onOpenSettings = { viewModel.deviceManager.openSystemSettingsPage() }
                        )
                        NavigationPanel.DIAGNOSTICS -> DiagnosticsPanel(
                            healthList = healthList,
                            isRunning = isRunningDiagnostics,
                            onRunDiagnostics = { viewModel.diagnosticsManager.runDiagnostics() }
                        )
                        NavigationPanel.UPDATES -> UpdatesPanel(
                            currentVersion = currentVersion,
                            stagedUpdate = stagedUpdate,
                            updateLogs = updateLogs,
                            onCheckUpdate = { viewModel.updateManager.checkForUpdates() },
                            onApplyUpdate = { viewModel.applySelfUpdate() },
                            onRollback = { viewModel.updateManager.rollbackToBackup() }
                        )
                        NavigationPanel.PLUGINS -> PluginsPanel(
                            plugins = plugins,
                            onTogglePlugin = { viewModel.pluginManager.togglePlugin(it) }
                        )
                        NavigationPanel.SETTINGS -> SettingsPanel()
                    }
                }
            }

            // Bottom Floating Voice & Command Bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(ZenithSurfaceDark)
                    .border(1.dp, ZenithSurfaceBorder)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Action Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPills.forEach { pill ->
                        SuggestionChip(
                            onClick = {
                                viewModel.textInput.value = pill
                                viewModel.processQuery(pill)
                            },
                            label = { Text(pill, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = ZenithSurfaceVariant,
                                labelColor = ZenithCyanPrimary
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = ZenithSurfaceBorder
                            )
                        )
                    }
                }

                // Command TextInput & Microphone Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Glowing Push-to-Talk Microphone Button
                    IconButton(
                        onClick = { viewModel.toggleMicListening() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isListening) ZenithGoldAction else ZenithCyanPrimary)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = ZenithBackgroundDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { viewModel.textInput.value = it },
                        placeholder = { Text("Command Zenith...", color = ZenithTextMuted, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZenithCyanPrimary,
                            unfocusedBorderColor = ZenithSurfaceBorder,
                            focusedTextColor = ZenithTextPrimary,
                            unfocusedTextColor = ZenithTextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                keyboardController?.hide()
                                viewModel.processQuery(textInput)
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.processQuery(textInput)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ZenithSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Command",
                            tint = ZenithCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Owner Approval Modal
            if (activeApprovalRequest != null) {
                OwnerApprovalDialog(
                    request = activeApprovalRequest!!,
                    onAuthorizeBiometric = { activity ->
                        viewModel.approvalManager.authenticateAndApprove(activity, activeApprovalRequest!!)
                    },
                    onDeny = {
                        viewModel.approvalManager.dismissAndReject(activeApprovalRequest!!)
                    }
                )
            }
        }
    }
}
