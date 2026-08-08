package com.example.ui

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.IntentType
import com.example.core.RiskLevel
import com.example.core.TaskStepStatus
import com.example.core.ZenithBrain
import com.example.core.ZenithParsedIntent
import com.example.data.AuditRepository
import com.example.data.MemoryEntity
import com.example.data.MemoryRepository
import com.example.data.ZenithDatabase
import com.example.device.CapabilityPermissionState
import com.example.device.DeviceCapabilityManager
import com.example.device.DeviceTelemetry
import com.example.diagnostics.SelfDiagnosticsManager
import com.example.hologram.HologramCoreState
import com.example.plugins.PluginManager
import com.example.security.ApprovalRequest
import com.example.security.OwnerApprovalManager
import com.example.updater.SelfUpdateManager
import com.example.voice.VoiceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class NavigationPanel {
    HUD,
    MEMORY,
    PERMISSIONS,
    DIAGNOSTICS,
    UPDATES,
    PLUGINS,
    SETTINGS
}

class ZenithViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ZenithDatabase.getDatabase(application)
    val memoryRepo = MemoryRepository(db.memoryDao())
    val auditRepo = AuditRepository(db.auditLogDao())

    val deviceManager = DeviceCapabilityManager(application)
    val voiceEngine = VoiceEngine(application)
    val brain = ZenithBrain(application, memoryRepo)

    val approvalManager = OwnerApprovalManager(application, auditRepo)
    val diagnosticsManager = SelfDiagnosticsManager(application, voiceEngine, deviceManager)
    val updateManager = SelfUpdateManager()
    val pluginManager = PluginManager()

    // UI States
    val hologramState = MutableStateFlow(HologramCoreState.IDLE)
    val activeIntent = MutableStateFlow<ZenithParsedIntent?>(null)
    val textInput = MutableStateFlow("")
    val activePanel = MutableStateFlow(NavigationPanel.HUD)

    val permissionStates = MutableStateFlow<Map<String, CapabilityPermissionState>>(emptyMap())
    val telemetryState = MutableStateFlow<DeviceTelemetry?>(null)

    val allMemories: StateFlow<List<MemoryEntity>> = memoryRepo.allMemories
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshPermissionsAndTelemetry()
        diagnosticsManager.runDiagnostics()

        // Observe voice recognizer output
        viewModelScope.launch {
            voiceEngine.recognizedText.collectLatest { text ->
                if (text.isNotEmpty()) {
                    textInput.value = text
                    processQuery(text)
                }
            }
        }

        // Observe voice state for hologram animation
        viewModelScope.launch {
            voiceEngine.isListening.collectLatest { listening ->
                if (listening) {
                    hologramState.value = HologramCoreState.LISTENING
                } else if (!voiceEngine.isSpeaking.value && hologramState.value == HologramCoreState.LISTENING) {
                    hologramState.value = HologramCoreState.IDLE
                }
            }
        }

        viewModelScope.launch {
            voiceEngine.isSpeaking.collectLatest { speaking ->
                if (speaking) {
                    hologramState.value = HologramCoreState.SPEAKING
                } else if (!voiceEngine.isListening.value && hologramState.value == HologramCoreState.SPEAKING) {
                    hologramState.value = HologramCoreState.IDLE
                }
            }
        }
    }

    fun refreshPermissionsAndTelemetry() {
        permissionStates.value = deviceManager.checkPermissionStates()
        telemetryState.value = deviceManager.getDeviceTelemetry()
    }

    fun processQuery(userQuery: String) {
        if (userQuery.isBlank()) return

        viewModelScope.launch {
            hologramState.value = HologramCoreState.THINKING
            val currentPerms = permissionStates.value.mapValues { it.value.isGranted }
            val intent = brain.processUserCommand(userQuery, currentPerms)
            activeIntent.value = intent

            // Log command into Memory DB
            memoryRepo.addMemory(
                category = "COMMAND",
                title = "User Query: ${userQuery.take(30)}",
                content = userQuery
            )

            // Evaluate Risk & Owner Approval
            if (intent.requiresConfirmation) {
                hologramState.value = HologramCoreState.WARNING
                approvalManager.requestApproval(
                    title = "Sensitive Action: ${intent.type.name}",
                    affectedResource = intent.targetAppOrFeature ?: "System Capabilities",
                    riskLevel = intent.riskLevel,
                    explanation = intent.confirmationMessage ?: "Owner Approval required to complete this action.",
                    onApproved = {
                        executeApprovedIntent(intent)
                    },
                    onRejected = {
                        hologramState.value = HologramCoreState.ERROR
                        voiceEngine.speak("Action authorization was denied by owner.")
                        viewModelScope.launch {
                            auditRepo.logAction(intent.type.name, intent.riskLevel.name, intent.targetAppOrFeature ?: "System", "REJECTED")
                        }
                    }
                )
            } else {
                executeApprovedIntent(intent)
            }
        }
    }

    private fun executeApprovedIntent(intent: ZenithParsedIntent) {
        viewModelScope.launch {
            hologramState.value = HologramCoreState.ACTION

            when (intent.type) {
                IntentType.LAUNCH_APP -> {
                    val appName = intent.targetAppOrFeature ?: "App"
                    val success = deviceManager.launchAppByName(appName)
                    if (success) {
                        hologramState.value = HologramCoreState.SUCCESS
                        voiceEngine.speak(intent.voiceResponse)
                    } else {
                        hologramState.value = HologramCoreState.ERROR
                        voiceEngine.speak("Unable to launch $appName. Verify app installation.")
                    }
                }
                IntentType.REMIND_ME -> {
                    deviceManager.sendNotification("ZENITH Reminder", intent.reminderText ?: "Scheduled task reminder")
                    memoryRepo.addMemory("TASK_HISTORY", "Reminder", intent.reminderText ?: "Scheduled task")
                    hologramState.value = HologramCoreState.SUCCESS
                    voiceEngine.speak(intent.voiceResponse)
                }
                IntentType.CHECK_PERMISSIONS -> {
                    activePanel.value = NavigationPanel.PERMISSIONS
                    hologramState.value = HologramCoreState.SUCCESS
                    voiceEngine.speak(intent.voiceResponse)
                }
                IntentType.SHOW_STATUS -> {
                    refreshPermissionsAndTelemetry()
                    activePanel.value = NavigationPanel.HUD
                    hologramState.value = HologramCoreState.SUCCESS
                    voiceEngine.speak(intent.voiceResponse)
                }
                IntentType.SELF_DIAGNOSTIC -> {
                    diagnosticsManager.runDiagnostics()
                    activePanel.value = NavigationPanel.DIAGNOSTICS
                    hologramState.value = HologramCoreState.SUCCESS
                    voiceEngine.speak(intent.voiceResponse)
                }
                IntentType.SELF_UPDATE -> {
                    updateManager.checkForUpdates()
                    activePanel.value = NavigationPanel.UPDATES
                    hologramState.value = HologramCoreState.SUCCESS
                    voiceEngine.speak(intent.voiceResponse)
                }
                else -> {
                    hologramState.value = HologramCoreState.SPEAKING
                    voiceEngine.speak(intent.voiceResponse)
                }
            }

            auditRepo.logAction(intent.type.name, intent.riskLevel.name, intent.targetAppOrFeature ?: "System", "EXECUTED", intent.voiceResponse)
        }
    }

    fun applySelfUpdate() {
        if (updateManager.applyUpdate()) {
            hologramState.value = HologramCoreState.SUCCESS
            voiceEngine.speak("Zenith core modules successfully updated to ${updateManager.currentVersion.value}.")
        }
    }

    fun toggleMicListening() {
        if (voiceEngine.isListening.value) {
            voiceEngine.stopListening()
        } else {
            voiceEngine.startListening()
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepo.deleteMemory(id)
        }
    }

    fun clearAllMemory() {
        viewModelScope.launch {
            memoryRepo.clearAllMemories()
            voiceEngine.speak("All local memories have been purged.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
    }
}

private fun <T> kotlinx.coroutines.flow.Flow<T>.stateIn(
    scope: kotlinx.coroutines.CoroutineScope,
    started: kotlinx.coroutines.flow.SharingStarted,
    initialValue: T
): StateFlow<T> {
    val state = MutableStateFlow(initialValue)
    scope.launch {
        this@stateIn.collect { state.value = it }
    }
    return state.asStateFlow()
}
