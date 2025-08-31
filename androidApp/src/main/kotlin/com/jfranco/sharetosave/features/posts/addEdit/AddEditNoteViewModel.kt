package com.jfranco.sharetosave.features.posts.addEdit

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class AddEditNoteViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel(), ContainerHost<AddEditNoteState, AddEditNoteSideEffect> {
    // create a container
    override val container = container<AddEditNoteState, AddEditNoteSideEffect>(
        initialState = AddEditNoteState(),
        savedStateHandle = savedStateHandle
    ) {
        coroutineScope {
            launch {
                snapshotFlow { state.title.text.text }.collectLatest { text ->
                    reduce {
                        state.copy(
                            title = state.title.copy(
                                text = TextFieldState(text.toString())
                            )
                        )
                    }
                }
            }

            launch {
                snapshotFlow { state.content.text.text }.collectLatest { text ->
                    reduce {
                        state.copy(
                            content = state.content.copy(
                                text = TextFieldState(text.toString())
                            )
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditNoteEvent) {
        Log.i("AddEditNoteViewModel", "Event: $event")

        when (event) {
            is AddEditNoteEvent.ChangeColor ->
                intent {
                    reduce {
                        state.copy(color = event.color)
                    }
                }

            is AddEditNoteEvent.ChangeContentFocus ->
                intent {
                    reduce {
                        val content = state.content.copy(
                            isHintVisible = !event.focusState.isFocused && state.content.text.text.isBlank()
                        )
                        state.copy(content = content)
                    }
                }

            is AddEditNoteEvent.ChangeTitleFocus ->
                intent {
                    reduce {
                        val title = state.title.copy(
                            isHintVisible = !event.focusState.isFocused && state.title.text.text.isBlank()
                        )
                        state.copy(title = title)
                    }
                }
            AddEditNoteEvent.SaveNote ->
                intent {
                    Log.i("AddEditNoteViewModel", "SaveNote: $state")
                }

        }
    }
}