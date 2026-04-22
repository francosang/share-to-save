package com.jfranco.sharetosave.features.posts.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jfranco.sharetosave.persistence.specification.NoteStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val notesStore: NoteStore
) : ViewModel(), ContainerHost<NotesState, NotesSideEffect> {
    override val container = container<NotesState, NotesSideEffect>(
        initialState = NotesState(),
        savedStateHandle = savedStateHandle
    ) {
        coroutineScope {
            launch {
                notesStore.observeNotes().collect { notes ->
                    reduce {
                        state.copy(notes = notes.sortedWith(state.noteOrder.comparator()))
                    }
                }
            }
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.AddEditNoteScreen ->
                intent {
                    postSideEffect(NotesSideEffect.NavigateToAddEditNoteScreen(event.note))
                }

            is NotesEvent.DeleteNote ->
                intent {
                    if (event.note.id == null) return@intent

                    notesStore.deleteNote(event.note.id)

                    reduce {
                        state.copy(recentDeletedNote = event.note)
                    }
                }

            is NotesEvent.Order ->
                intent {
                    reduce {
                        state.copy(
                            noteOrder = event.noteOrder,
                            notes = state.notes.sortedWith(event.noteOrder.comparator())
                        )
                    }
                }

            NotesEvent.RestoreNote ->
                intent {
                    val note = state.recentDeletedNote ?: return@intent

                    notesStore.save(note)

                    reduce {
                        state.copy(recentDeletedNote = null)
                    }
                }

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