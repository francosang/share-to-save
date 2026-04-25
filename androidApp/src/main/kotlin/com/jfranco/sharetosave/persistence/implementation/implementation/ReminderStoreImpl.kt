package com.jfranco.sharetosave.persistence.implementation.implementation

import com.jfranco.sharetosave.di.IoDispatcher
import com.jfranco.sharetosave.domain.Reminder
import com.jfranco.sharetosave.persistence.implementation.dao.ReminderDao
import com.jfranco.sharetosave.persistence.entity.toDomains
import com.jfranco.sharetosave.persistence.entity.toEntity
import com.jfranco.sharetosave.persistence.specification.ReminderStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReminderStoreImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ReminderStore {

    override fun observeReminders(): Flow<List<Reminder>> =
        reminderDao.observeAll()
            .map { it.toDomains() }
            .flowOn(ioDispatcher)

    override suspend fun save(reminder: Reminder): Reminder = withContext(ioDispatcher) {
        if (reminder.id == null) {
            val id = reminderDao.insert(reminder.toEntity())
            reminder.copy(id = id)
        } else {
            reminderDao.insert(reminder.toEntity())
            reminder
        }
    }

    override suspend fun delete(id: Long) = withContext(ioDispatcher) {
        reminderDao.delete(id)
    }

    override suspend fun getRemindersForNote(noteId: Long): List<Reminder> =
        withContext(ioDispatcher) {
            reminderDao.getForNote(noteId).toDomains()
        }

    override suspend fun getAllActive(): List<Reminder> = withContext(ioDispatcher) {
        reminderDao.getAllActive().toDomains()
    }
}
