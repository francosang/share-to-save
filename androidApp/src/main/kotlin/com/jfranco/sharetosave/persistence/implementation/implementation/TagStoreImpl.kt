package com.jfranco.sharetosave.persistence.implementation.implementation

import com.jfranco.sharetosave.di.IoDispatcher
import com.jfranco.sharetosave.domain.Tag
import com.jfranco.sharetosave.persistence.implementation.dao.TagDao
import com.jfranco.sharetosave.persistence.entity.toDomain
import com.jfranco.sharetosave.persistence.entity.toDomains
import com.jfranco.sharetosave.persistence.entity.toEntity
import com.jfranco.sharetosave.persistence.specification.TagStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagStoreImpl @Inject constructor(
    private val tagDao: TagDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TagStore {

    override fun observeTags(): Flow<List<Tag>> {
        return tagDao.observeAll()
            .map { it.toDomains() }
            .flowOn(ioDispatcher)
    }

    override suspend fun save(tag: Tag): Tag = withContext(ioDispatcher) {
        if (tag.id == null) {
            val id = tagDao.insert(tag.toEntity())
            tag.copy(id = id)
        } else {
            tagDao.update(tag.toEntity())
            tag
        }
    }

    override suspend fun delete(id: Long) = withContext(ioDispatcher) {
        tagDao.delete(id)
    }

    override suspend fun getTags(): List<Tag> = withContext(ioDispatcher) {
        tagDao.selectAll().toDomains()
    }

    override suspend fun getTagsForNote(noteId: Long): List<Tag> = withContext(ioDispatcher) {
        tagDao.getTagsForNote(noteId).toDomains()
    }
}
