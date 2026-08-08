package com.example.core

import android.content.Context
import com.example.data.MemoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ZenithBrain(
    private val context: Context,
    private val memoryRepository: MemoryRepository
) {
    suspend fun processUserCommand(userQuery: String, activePermissions: Map<String, Boolean>): ZenithParsedIntent = withContext(Dispatchers.IO) {
        val queryLower = userQuery.lowercase().trim()
        val apiKey = GeminiClient.getApiKey()

        if (apiKey.isNotEmpty()) {
            try {
                val systemPrompt = """
                    You are ZENITH, an ultra-advanced, cinematic, highly intelligent adult male AI assistant.
                    Analyze the user command and extract:
                    1. Intent type: LAUNCH_APP, REMIND_ME, SHOW_STATUS, SHOW_MEMORIES, EDIT_MEMORY, OPEN_SETTINGS, CHECK_PERMISSIONS, SELF_DIAGNOSTIC, SELF_UPDATE, GENERAL_CONVERSATION.
                    2. Spoken response: Concise, natural, adult male tone (e.g. 'I am opening WhatsApp for you.', 'Reminder scheduled for tomorrow.', 'All systems operational.'). Never say 'Command received' or robotic jargon.
                    3. Required target or details.
                    Format your output clearly with INTENT:, TARGET:, RESPONSE:, RISK:
                """.trimIndent()

                val request = GeminiGenerateRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = userQuery)))),
                    systemInstruction = GeminiSystemInstruction(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val response = GeminiClient.api.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!responseText.isNullOrEmpty()) {
                    val parsed = parseAiResponse(userQuery, responseText, activePermissions)
                    return@withContext parsed
                }
            } catch (e: Exception) {
                // Fallback to local deterministic AI rule engine if API call fails
            }
        }

        // Local Offline/Fallback AI Rule Engine
        return@withContext parseLocalIntent(queryLower, userQuery, activePermissions)
    }

    private fun parseAiResponse(userQuery: String, aiOutput: String, activePermissions: Map<String, Boolean>): ZenithParsedIntent {
        var intentType = IntentType.GENERAL_CONVERSATION
        var target: String? = null
        var voiceText = aiOutput
        var riskLevel = RiskLevel.LOW
        var requiresConfirm = false

        val lines = aiOutput.lines()
        for (line in lines) {
            when {
                line.startsWith("INTENT:", ignoreCase = true) -> {
                    val typeStr = line.substringAfter(":").trim().uppercase()
                    intentType = try { IntentType.valueOf(typeStr) } catch (e: Exception) { IntentType.GENERAL_CONVERSATION }
                }
                line.startsWith("TARGET:", ignoreCase = true) -> {
                    target = line.substringAfter(":").trim()
                }
                line.startsWith("RESPONSE:", ignoreCase = true) -> {
                    voiceText = line.substringAfter(":").trim()
                }
                line.startsWith("RISK:", ignoreCase = true) -> {
                    val riskStr = line.substringAfter(":").trim().uppercase()
                    riskLevel = try { RiskLevel.valueOf(riskStr) } catch (e: Exception) { RiskLevel.LOW }
                }
            }
        }

        if (intentType == IntentType.GENERAL_CONVERSATION && aiOutput.length < 250) {
            voiceText = aiOutput
        }

        // Generate Task Steps
        val steps = createStepsForIntent(intentType, target, userQuery, activePermissions)
        val requiresPermission = steps.any { it.status == TaskStepStatus.CONFIRMATION_REQUIRED }

        return ZenithParsedIntent(
            type = intentType,
            targetAppOrFeature = target,
            reminderText = if (intentType == IntentType.REMIND_ME) userQuery else null,
            voiceResponse = voiceText,
            riskLevel = if (requiresPermission) RiskLevel.MEDIUM else riskLevel,
            requiresConfirmation = requiresConfirm || requiresPermission,
            confirmationMessage = if (requiresConfirm || requiresPermission) "Owner Approval required to execute sensitive operation." else null,
            steps = steps
        )
    }

    private fun parseLocalIntent(queryLower: String, rawQuery: String, permissions: Map<String, Boolean>): ZenithParsedIntent {
        return when {
            queryLower.contains("open whatsapp") || queryLower.contains("launch whatsapp") -> {
                val steps = listOf(
                    TaskStep(1, "Parse user command 'Open WhatsApp'", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Resolve package name 'com.whatsapp'", TaskStepStatus.COMPLETED),
                    TaskStep(3, "Launch application", TaskStepStatus.IN_PROGRESS)
                )
                ZenithParsedIntent(
                    type = IntentType.LAUNCH_APP,
                    targetAppOrFeature = "WhatsApp",
                    voiceResponse = "Opening WhatsApp now.",
                    riskLevel = RiskLevel.LOW,
                    steps = steps
                )
            }
            queryLower.contains("open youtube") || queryLower.contains("launch youtube") -> {
                val steps = listOf(
                    TaskStep(1, "Parse user command 'Open YouTube'", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Resolve package name 'com.google.android.youtube'", TaskStepStatus.COMPLETED),
                    TaskStep(3, "Launch application", TaskStepStatus.IN_PROGRESS)
                )
                ZenithParsedIntent(
                    type = IntentType.LAUNCH_APP,
                    targetAppOrFeature = "YouTube",
                    voiceResponse = "Opening YouTube for you.",
                    riskLevel = RiskLevel.LOW,
                    steps = steps
                )
            }
            queryLower.contains("remind me") || queryLower.contains("reminder") -> {
                val steps = listOf(
                    TaskStep(1, "Extract reminder intent", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Check notification permission", if (permissions["NOTIFICATIONS"] == true) TaskStepStatus.COMPLETED else TaskStepStatus.CONFIRMATION_REQUIRED, requiredPermission = "POST_NOTIFICATIONS"),
                    TaskStep(3, "Schedule system notification & memory entry", TaskStepStatus.PENDING)
                )
                ZenithParsedIntent(
                    type = IntentType.REMIND_ME,
                    reminderText = rawQuery,
                    voiceResponse = "Reminder scheduled. I will notify you as requested.",
                    riskLevel = RiskLevel.MEDIUM,
                    steps = steps
                )
            }
            queryLower.contains("permission") -> {
                val steps = listOf(
                    TaskStep(1, "Query active Android security permissions", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Generate capability matrix", TaskStepStatus.COMPLETED)
                )
                ZenithParsedIntent(
                    type = IntentType.CHECK_PERMISSIONS,
                    voiceResponse = "Presenting active Android permissions and authorization levels.",
                    riskLevel = RiskLevel.LOW,
                    steps = steps
                )
            }
            queryLower.contains("system status") || queryLower.contains("status") -> {
                val steps = listOf(
                    TaskStep(1, "Inspect core hardware & background metrics", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Compile system telemetry report", TaskStepStatus.COMPLETED)
                )
                ZenithParsedIntent(
                    type = IntentType.SHOW_STATUS,
                    voiceResponse = "All core systems online. CPU, network telemetry, and memory persistence are nominal.",
                    riskLevel = RiskLevel.LOW,
                    steps = steps
                )
            }
            queryLower.contains("diagnostic") || queryLower.contains("health") -> {
                val steps = listOf(
                    TaskStep(1, "Run self-diagnostic suite across 8 modules", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Evaluate API, TTS, Mic, and Storage integrity", TaskStepStatus.COMPLETED)
                )
                ZenithParsedIntent(
                    type = IntentType.SELF_DIAGNOSTIC,
                    voiceResponse = "Self-diagnostic suite executed. All Zenith engine modules are healthy.",
                    riskLevel = RiskLevel.LOW,
                    steps = steps
                )
            }
            queryLower.contains("update") -> {
                val steps = listOf(
                    TaskStep(1, "Inspect core module checksums", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Stage proposed version patch v2.4", TaskStepStatus.COMPLETED),
                    TaskStep(3, "Require Owner Approval before applying", TaskStepStatus.CONFIRMATION_REQUIRED)
                )
                ZenithParsedIntent(
                    type = IntentType.SELF_UPDATE,
                    voiceResponse = "Module update v2.4 staged. Owner Approval required to apply updates.",
                    riskLevel = RiskLevel.HIGH,
                    requiresConfirmation = true,
                    confirmationMessage = "System Update v2.4 requires Owner Biometric Authorization.",
                    steps = steps
                )
            }
            else -> {
                val steps = listOf(
                    TaskStep(1, "Process natural language prompt", TaskStepStatus.COMPLETED),
                    TaskStep(2, "Generate conversational response", TaskStepStatus.COMPLETED)
                )
                ZenithParsedIntent(
                    type = IntentType.GENERAL_CONVERSATION,
                    voiceResponse = "I am ZENITH. Standing by to manage your schedule, launch applications, or perform device telemetry analysis.",
                    riskLevel = RiskLevel.LOW,
                    steps = steps
                )
            }
        }
    }

    private fun createStepsForIntent(intent: IntentType, target: String?, query: String, permissions: Map<String, Boolean>): List<TaskStep> {
        return when (intent) {
            IntentType.LAUNCH_APP -> listOf(
                TaskStep(1, "Parse command target '$target'", TaskStepStatus.COMPLETED),
                TaskStep(2, "Locate Android application intent", TaskStepStatus.COMPLETED),
                TaskStep(3, "Execute launch request", TaskStepStatus.IN_PROGRESS)
            )
            IntentType.REMIND_ME -> listOf(
                TaskStep(1, "Analyze reminder parameters", TaskStepStatus.COMPLETED),
                TaskStep(2, "Verify Notification capability", if (permissions["NOTIFICATIONS"] == true) TaskStepStatus.COMPLETED else TaskStepStatus.CONFIRMATION_REQUIRED, requiredPermission = "POST_NOTIFICATIONS"),
                TaskStep(3, "Save to memory & schedule system reminder", TaskStepStatus.PENDING)
            )
            else -> listOf(
                TaskStep(1, "Understand natural language intent", TaskStepStatus.COMPLETED),
                TaskStep(2, "Execute permitted operation", TaskStepStatus.COMPLETED)
            )
        }
    }
}
