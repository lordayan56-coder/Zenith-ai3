package com.example.data

import kotlinx.coroutines.flow.Flow

class AuditRepository(private val auditLogDao: AuditLogDao) {
    val allLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    suspend fun logAction(actionName: String, riskLevel: String, resource: String, status: String, details: String = ""): Long {
        val log = AuditLogEntity(
            actionName = actionName,
            riskLevel = riskLevel,
            resource = resource,
            status = status,
            details = details
        )
        return auditLogDao.insertAuditLog(log)
    }

    suspend fun clearAuditLogs() {
        auditLogDao.clearAuditLogs()
    }
}
