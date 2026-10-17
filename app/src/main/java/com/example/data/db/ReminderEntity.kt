package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val timeFormatted: String = "",
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
