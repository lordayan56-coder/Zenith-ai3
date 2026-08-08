package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.TaskStepStatus
import com.example.core.ZenithParsedIntent
import com.example.device.DeviceTelemetry
import com.example.ui.theme.*

@Composable
fun HudPanel(
    intent: ZenithParsedIntent?,
    telemetry: DeviceTelemetry?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Task Steps Timeline
        if (intent != null && intent.steps.isNotEmpty()) {
            Surface(
                color = ZenithSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZenithSurfaceBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TASK TIMELINE Execution Plan",
                        color = ZenithCyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    intent.steps.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = when (step.status) {
                                                TaskStepStatus.COMPLETED -> ZenithGreenSuccess
                                                TaskStepStatus.IN_PROGRESS -> ZenithCyanPrimary
                                                TaskStepStatus.CONFIRMATION_REQUIRED -> ZenithGoldAction
                                                TaskStepStatus.FAILED -> ZenithRedAlert
                                                else -> ZenithTextMuted
                                            },
                                            shape = RoundedCornerShape(5.dp)
                                        )
                                )
                                Text(
                                    text = step.description,
                                    color = ZenithTextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = step.status.name,
                                color = when (step.status) {
                                    TaskStepStatus.COMPLETED -> ZenithGreenSuccess
                                    TaskStepStatus.CONFIRMATION_REQUIRED -> ZenithGoldAction
                                    else -> ZenithTextSecondary
                                },
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Telemetry Grid
        if (telemetry != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryCard(
                    title = "BATTERY",
                    value = "${telemetry.batteryPercentage}%",
                    subValue = if (telemetry.isCharging) "Charging" else "Discharging",
                    icon = Icons.Default.BatteryFull,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "NETWORK",
                    value = telemetry.networkStatus,
                    subValue = "Online",
                    icon = Icons.Default.NetworkCheck,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryCard(
                    title = "STORAGE",
                    value = "${telemetry.storageAvailableGb} GB",
                    subValue = "Free of ${telemetry.storageTotalGb} GB",
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "RAM / UPTIME",
                    value = "${telemetry.activeMemoryMb} MB",
                    subValue = "${telemetry.systemUptimeHours}h uptime",
                    icon = Icons.Default.Memory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ZenithSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, ZenithSurfaceBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ZenithCyanPrimary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    color = ZenithTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    color = ZenithTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subValue,
                    color = ZenithTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
