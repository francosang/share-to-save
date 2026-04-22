package com.jfranco.sharetosave.persistence.implementation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfranco.sharetosave.persistence.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    suspend fun selectAll(): List<NoteEntity>

    @Query("SELECT * FROM note")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun selectNote(id: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun delete(id: Int)
}
