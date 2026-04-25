package com.jfranco.sharetosave.persistence.implementation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfranco.sharetosave.persistence.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tag")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag")
    suspend fun selectAll(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Query("DELETE FROM tag WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
        SELECT t.* FROM tag t
        INNER JOIN notes_tags ref ON t.id = ref.tag_id
        WHERE ref.note_id = :noteId
    """)
    suspend fun getTagsForNote(noteId: Long): List<TagEntity>
}
