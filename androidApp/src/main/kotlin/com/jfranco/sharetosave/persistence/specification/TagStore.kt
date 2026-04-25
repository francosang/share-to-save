package com.jfranco.sharetosave.persistence.specification

import com.jfranco.sharetosave.domain.Tag
import kotlinx.coroutines.flow.Flow

interface TagStore {
    fun observeTags(): Flow<List<Tag>>
    suspend fun save(tag: Tag): Tag
    suspend fun delete(id: Long)
    suspend fun getTags(): List<Tag>
    suspend fun getTagsForNote(noteId: Long): List<Tag>
}
