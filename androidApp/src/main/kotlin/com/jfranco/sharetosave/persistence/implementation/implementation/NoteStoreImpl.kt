package com.jfranco.sharetosave.persistence.implementation.implementation

import com.jfranco.sharetosave.di.IoDispatcher
import com.jfranco.sharetosave.domain.Note
import com.jfranco.sharetosave.persistence.implementation.dao.NoteDao
import com.jfranco.sharetosave.persistence.entity.toDomain
import com.jfranco.sharetosave.persistence.entity.toDomains
import com.jfranco.sharetosave.persistence.entity.toEntity
import com.jfranco.sharetosave.persistence.specification.NoteStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NoteStoreImpl @Inject constructor(
    private val noteDao: NoteDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NoteStore {

    override suspend fun getNotes(): List<Note> = withContext(ioDispatcher) {
        noteDao.selectAll().toDomains()
    }

    override fun observeNotes(): Flow<List<Note>> {
        return noteDao.observeAll()
            .map { it.toDomains() }
            .flowOn(ioDispatcher)
    }

    override suspend fun save(note: Note): Note = withContext(ioDispatcher) {
        val entity = note.toEntity()
        val id = noteDao.insert(entity)
        note.copy(id = id)
    }

    override suspend fun getNote(id: Long): Note? = withContext(ioDispatcher) {
        noteDao.selectNote(id)?.toDomain()
    }

    override suspend fun deleteNote(id: Long) = withContext(ioDispatcher) {
        noteDao.delete(id)
    }
}
