package com.example.data.repository

import com.example.data.db.JarvisDao
import com.example.data.db.ReminderEntity
import kotlinx.coroutines.flow.Flow

class RemindersRepository(private val dao: JarvisDao) {

    val reminders: Flow<List<ReminderEntity>> = dao.getAllReminders()

    suspend fun addReminder(title: String, description: String, time: String, priority: String) {
        dao.insertReminder(
            ReminderEntity(
                title = title,
                description = description,
                timeFormatted = time,
                priority = priority,
                isCompleted = false
            )
        )
    }

    suspend fun toggleReminderStatus(reminder: ReminderEntity) {
        dao.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
    }

    suspend fun deleteReminder(id: Long) {
        dao.deleteReminder(id)
    }
}
