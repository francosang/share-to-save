package com.jfranco.sharetosave.di

import com.jfranco.sharetosave.persistence.implementation.implementation.NoteStoreImpl
import com.jfranco.sharetosave.persistence.specification.NoteStore
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
}
