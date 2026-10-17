package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_memories")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyCategory: String, // "USER_NAME", "PREFERENCE", "PROJECT", "GENERAL_FACT"
    val memoryContent: String,
    val timestamp: Long = System.currentTimeMillis()
)
