package com.jfranco.sharetosave.di

import com.jfranco.sharetosave.persistence.implementation.implementation.NoteStoreImpl
import com.jfranco.sharetosave.persistence.implementation.implementation.ReminderStoreImpl
import com.jfranco.sharetosave.persistence.implementation.implementation.TagStoreImpl
import com.jfranco.sharetosave.persistence.specification.NoteStore
import com.jfranco.sharetosave.persistence.specification.ReminderStore
import com.jfranco.sharetosave.persistence.specification.TagStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class StoreModuleBinds {

    @Singleton
    @Binds
    abstract fun bindNoteStore(impl: NoteStoreImpl): NoteStore

    @Singleton
    @Binds
    abstract fun bindTagStore(impl: TagStoreImpl): TagStore

    @Singleton
    @Binds
    abstract fun bindReminderStore(impl: ReminderStoreImpl): ReminderStore
}
