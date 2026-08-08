package com.example.core

enum class IntentType {
    LAUNCH_APP,
    REMIND_ME,
    SHOW_STATUS,
    SHOW_MEMORIES,
    EDIT_MEMORY,
    OPEN_SETTINGS,
    CHECK_PERMISSIONS,
    SELF_DIAGNOSTIC,
    SELF_UPDATE,
    GENERAL_CONVERSATION,
    UNKNOWN
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class TaskStep(
    val id: Int,
    val description: String,
    val status: TaskStepStatus = TaskStepStatus.PENDING,
    val requiredPermission: String? = null
)

enum class TaskStepStatus {
    PENDING,
    IN_PROGRESS,
    CONFIRMATION_REQUIRED,
    COMPLETED,
    FAILED
}

data class ZenithParsedIntent(
    val type: IntentType,
    val targetAppOrFeature: String? = null,
    val reminderText: String? = null,
    val reminderTimeText: String? = null,
    val voiceResponse: String,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val requiresConfirmation: Boolean = false,
    val confirmationMessage: String? = null,
    val requiredPermissions: List<String> = emptyList(),
    val steps: List<TaskStep> = emptyList()
)
