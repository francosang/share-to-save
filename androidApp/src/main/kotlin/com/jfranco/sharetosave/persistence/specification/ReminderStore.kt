package com.jfranco.sharetosave.persistence.specification

import com.jfranco.sharetosave.domain.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderStore {
    fun observeReminders(): Flow<List<Reminder>>
    suspend fun save(reminder: Reminder): Reminder
    suspend fun delete(id: Long)
    suspend fun getRemindersForNote(noteId: Long): List<Reminder>
    suspend fun getAllActive(): List<Reminder>
}
