package com.jfranco.sharetosave.persistence.implementation.implementation

import com.jfranco.sharetosave.domain.Note
import com.jfranco.sharetosave.persistence.implementation.dao.NoteDao
import com.jfranco.sharetosave.persistence.entity.toDomain
import com.jfranco.sharetosave.persistence.entity.toDomains
import com.jfranco.sharetosave.persistence.entity.toEntity
import com.jfranco.sharetosave.persistence.specification.NoteStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteStoreImpl @Inject constructor(
    private val noteDao: NoteDao,
) : NoteStore {

    override suspend fun getNotes(): List<Note> {
        return noteDao.selectAll().toDomains()
    }

    override fun observeNotes(): Flow<List<Note>> {
        return noteDao.observeAll().map {
            it.toDomains()
        }
    }

    override suspend fun save(note: Note): Note {
        val entity = note.toEntity()
        val id = noteDao.insert(entity)
        return note.copy(id = id.toInt())
    }

    override suspend fun getNote(id: Int): Note? {
        val entity = noteDao.selectNote(id)
        return entity?.toDomain()
    }

    override suspend fun deleteNote(id: Int) {
        noteDao.delete(id)
    }
}
