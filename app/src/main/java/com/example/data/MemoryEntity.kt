package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // CONVERSATION, PREFERENCE, PROJECT, DEVICE, COMMAND, CAPABILITY, TEMPORARY, TASK_HISTORY
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEditable: Boolean = true,
    val isPinned: Boolean = false
)
