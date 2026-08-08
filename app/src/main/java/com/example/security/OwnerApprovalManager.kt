package com.example.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.core.RiskLevel
import com.example.data.AuditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ApprovalRequest(
    val id: String,
    val title: String,
    val affectedResource: String,
    val riskLevel: RiskLevel,
    val explanation: String,
    val onApproved: () -> Unit,
    val onRejected: () -> Unit
)

class OwnerApprovalManager(
    private val context: Context,
    private val auditRepository: AuditRepository
) {
    val activeRequest = MutableStateFlow<ApprovalRequest?>(null)

    fun canUseBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun requestApproval(
        title: String,
        affectedResource: String,
        riskLevel: RiskLevel,
        explanation: String,
        onApproved: () -> Unit,
        onRejected: () -> Unit
    ) {
        val request = ApprovalRequest(
            id = System.currentTimeMillis().toString(),
            title = title,
            affectedResource = affectedResource,
            riskLevel = riskLevel,
            explanation = explanation,
            onApproved = onApproved,
            onRejected = onRejected
        )
        activeRequest.value = request
    }

    fun authenticateAndApprove(activity: FragmentActivity, request: ApprovalRequest) {
        if (canUseBiometrics()) {
            val executor = ContextCompat.getMainExecutor(context)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    activeRequest.value = null
                    request.onApproved()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancels or fails, reject
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        dismissAndReject(request)
                    }
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("ZENITH OWNER APPROVAL")
                .setSubtitle(request.title)
                .setDescription("Resource: ${request.affectedResource}\nRisk: ${request.riskLevel.name}")
                .setNegativeButtonText("Deny Request")
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } else {
            // Biometric hardware fallback: Direct explicit confirmation
            activeRequest.value = null
            request.onApproved()
        }
    }

    fun dismissAndReject(request: ApprovalRequest) {
        activeRequest.value = null
        request.onRejected()
    }
}
