package com.jfranco.sharetosave.persistence.implementation

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jfranco.sharetosave.persistence.implementation.converter.LocalDateTimeConverter
import com.jfranco.sharetosave.persistence.implementation.dao.NoteDao
import com.jfranco.sharetosave.persistence.entity.NoteEntity

const val DB_VERSION_1 = 1

@Database(
    entities = [
        NoteEntity::class,
    ],
    version = DB_VERSION_1
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
}
