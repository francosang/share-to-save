package com.jfranco.sharetosave.features.tags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jfranco.sharetosave.di.IoDispatcher
import com.jfranco.sharetosave.domain.Tag
import com.jfranco.sharetosave.persistence.specification.TagStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tagStore: TagStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), ContainerHost<TagsState, Nothing> {

    override val container = container<TagsState, Nothing>(
        initialState = TagsState(),
        savedStateHandle = savedStateHandle
    ) {
        coroutineScope {
            launch {
                tagStore.observeTags().collect { tags ->
                    reduce { state.copy(tags = tags) }
                }
            }
        }
    }

    fun onEvent(event: TagsEvent) {
        when (event) {
            is TagsEvent.AddTag ->
                intent {
                    withContext(ioDispatcher) {
                        tagStore.save(Tag(id = null, name = event.name, color = event.color))
                    }
                }

            is TagsEvent.DeleteTag ->
                intent {
                    withContext(ioDispatcher) {
                        tagStore.delete(event.id)
                    }
                }

            is TagsEvent.EditTag ->
                intent {
                    withContext(ioDispatcher) {
                        tagStore.save(event.tag)
                    }
                }
        }
    }
}
