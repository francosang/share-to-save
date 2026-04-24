package com.jfranco.sharetosave.di

import android.content.Context
import androidx.room.Room
import com.jfranco.sharetosave.persistence.implementation.AppDatabase
import com.jfranco.sharetosave.persistence.implementation.MIGRATION_1_2
import com.jfranco.sharetosave.persistence.implementation.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    private val databaseName = "noteapp-database"

    @Singleton
    @Provides
    fun providesDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, databaseName
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Singleton
    @Provides
    fun providesNoteDao(appDatabase: AppDatabase): NoteDao {
        return appDatabase.getNoteDao()
    }
}
