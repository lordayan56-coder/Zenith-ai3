package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plugins.ZenithPlugin
import com.example.ui.theme.*

@Composable
fun PluginsPanel(
    plugins: List<ZenithPlugin>,
    onTogglePlugin: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = "Plugins",
                tint = ZenithCyanPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "DYNAMIC CAPABILITIES & PLUGINS (${plugins.size})",
                color = ZenithCyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        plugins.forEach { plugin ->
            Surface(
                color = ZenithSurfaceDark,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZenithSurfaceBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = plugin.name,
                                color = ZenithTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "[${plugin.category}]",
                                color = ZenithCyanPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = plugin.description,
                            color = ZenithTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = plugin.isEnabled,
                        onCheckedChange = { onTogglePlugin(plugin.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ZenithCyanPrimary,
                            checkedTrackColor = ZenithSurfaceVariant,
                            uncheckedThumbColor = ZenithTextMuted,
                            uncheckedTrackColor = ZenithSurfaceDark
                        )
                    )
                }
            }
        }
    }
}
