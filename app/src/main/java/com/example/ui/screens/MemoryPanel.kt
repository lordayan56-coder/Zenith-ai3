package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MemoryEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryPanel(
    memories: List<MemoryEntity>,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemory: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "COMMAND", "PREFERENCE", "PROJECT", "DEVICE", "TASK_HISTORY")
    val filtered = if (selectedCategory == "ALL") memories else memories.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Memory Core",
                    tint = ZenithCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "ZENITH MEMORY INDEX (${memories.size})",
                    color = ZenithCyanPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (memories.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearAllMemory,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenithRedAlert),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenithRedAlert),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "CLEAR ALL", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.take(4).forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ZenithCyanPrimary,
                        selectedLabelColor = ZenithBackgroundDark,
                        containerColor = ZenithSurfaceDark,
                        labelColor = ZenithTextSecondary
                    )
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stored memories found in this category.",
                    color = ZenithTextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { memory ->
                    MemoryItemRow(memory = memory, onDelete = { onDeleteMemory(memory.id) })
                }
            }
        }
    }
}

@Composable
private fun MemoryItemRow(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("HH:mm:ss dd MMM", Locale.US).format(Date(memory.timestamp))

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
                        text = memory.category,
                        color = ZenithCyanPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = dateStr,
                        color = ZenithTextMuted,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = memory.title,
                    color = ZenithTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = memory.content,
                    color = ZenithTextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Memory",
                    tint = ZenithRedAlert,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
