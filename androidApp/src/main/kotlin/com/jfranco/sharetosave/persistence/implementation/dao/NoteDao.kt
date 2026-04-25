package com.jfranco.sharetosave.persistence.implementation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfranco.sharetosave.persistence.entity.NoteEntity
import com.jfranco.sharetosave.persistence.entity.NoteTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    suspend fun selectAll(): List<NoteEntity>

    @Query("SELECT * FROM note")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun selectNote(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
        SELECT n.* FROM note n
        INNER JOIN notes_tags ref ON n.id = ref.note_id
        WHERE ref.tag_id = :tagId
    """)
    fun observeByTag(tagId: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM notes_tags WHERE note_id = :noteId")
    suspend fun deleteCrossRefsForNote(noteId: Long)
}
