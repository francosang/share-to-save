package com.jfranco.sharetosave.features.posts.list

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class NotesViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel(), ContainerHost<NotesState, NotesSideEffect> {
    // create a container
    override val container = container<NotesState, NotesSideEffect>(
        initialState = NotesState(),
        savedStateHandle = savedStateHandle
    )

    fun onEvent(event: NotesEvent) {
        Log.i("NotesViewModel", "Event: $event")

        when (event) {
            is NotesEvent.AddEditNoteScreen ->
                intent {
                    postSideEffect(NotesSideEffect.NavigateToAddEditNoteScreen(event.note))
                }

            is NotesEvent.DeleteNote -> TODO()

            is NotesEvent.Order ->
                intent {
                    reduce {
                        state.copy(noteOrder = event.noteOrder)
                    }
                }

            NotesEvent.RestoreNote -> TODO()

            NotesEvent.ToggleOrderSection ->
                intent {
                    reduce {
                        state.copy(
                            isOrderSectionVisible = !state.isOrderSectionVisible
                        )
                    }
                }
        }
    }
}