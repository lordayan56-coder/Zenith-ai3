package com.example.data

import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntity>> {
        return memoryDao.getMemoriesByCategory(category)
    }

    suspend fun addMemory(category: String, title: String, content: String, isEditable: Boolean = true): Long {
        val memory = MemoryEntity(
            category = category,
            title = title,
            content = content,
            isEditable = isEditable
        )
        return memoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(memory: MemoryEntity) {
        memoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }
}
