package com.jfranco.sharetosave.persistence.implementation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfranco.sharetosave.persistence.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Query("DELETE FROM reminder WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM reminder WHERE note_id = :noteId")
    suspend fun getForNote(noteId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminder")
    suspend fun getAllActive(): List<ReminderEntity>
}
