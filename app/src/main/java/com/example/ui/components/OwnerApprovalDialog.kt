package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.core.RiskLevel
import com.example.security.ApprovalRequest
import com.example.ui.theme.*

@Composable
fun OwnerApprovalDialog(
    request: ApprovalRequest,
    onAuthorizeBiometric: (FragmentActivity) -> Unit,
    onDeny: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDeny,
        containerColor = ZenithSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .border(2.dp, ZenithGoldAction, RoundedCornerShape(20.dp)),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security Shield",
                    tint = ZenithGoldAction,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "OWNER APPROVAL REQUIRED",
                        color = ZenithGoldAction,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = request.title,
                        color = ZenithTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Risk Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when (request.riskLevel) {
                                RiskLevel.CRITICAL -> ZenithRedAlert.copy(alpha = 0.2f)
                                RiskLevel.HIGH -> ZenithGoldAction.copy(alpha = 0.2f)
                                else -> ZenithCyanPrimary.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AFFECTED RESOURCE:",
                        color = ZenithTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = request.affectedResource,
                        color = ZenithTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = request.explanation,
                    color = ZenithTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = ZenithRedAlert,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RISK LEVEL: ${request.riskLevel.name}",
                        color = ZenithRedAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (context is FragmentActivity) {
                        onAuthorizeBiometric(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZenithCyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Pass",
                    tint = ZenithBackgroundDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AUTHORIZE",
                    color = ZenithBackgroundDark,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDeny,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenithRedAlert),
                border = BorderStroke(1.dp, ZenithRedAlert),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "REJECT",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}
