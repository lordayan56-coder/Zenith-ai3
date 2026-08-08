package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionName: String,
    val riskLevel: String, // LOW, MEDIUM, HIGH, CRITICAL
    val resource: String,
    val status: String, // REQUESTED, APPROVED, REJECTED, EXECUTED, FAILED
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
