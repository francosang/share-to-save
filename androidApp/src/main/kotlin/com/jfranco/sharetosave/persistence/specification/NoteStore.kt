package com.jfranco.sharetosave.persistence.specification

import com.jfranco.sharetosave.domain.Note
import kotlinx.coroutines.flow.Flow

interface NoteStore {
    suspend fun getNotes(): List<Note>
    fun observeNotes(): Flow<List<Note>>
    suspend fun save(note: Note): Note
    suspend fun getNote(id: Long): Note?
    suspend fun deleteNote(id: Long)
    suspend fun setNoteTags(noteId: Long, tagIds: List<Long>)
    fun observeNotesByTag(tagId: Long): Flow<List<Note>>
}
