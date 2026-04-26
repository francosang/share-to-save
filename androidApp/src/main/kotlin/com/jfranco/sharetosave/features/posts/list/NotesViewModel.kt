package com.jfranco.sharetosave.features.posts.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jfranco.sharetosave.domain.ReminderWithNote
import com.jfranco.sharetosave.features.posts.list.NotesSideEffect.*
import com.jfranco.sharetosave.persistence.specification.NoteStore
import com.jfranco.sharetosave.persistence.specification.ReminderStore
import com.jfranco.sharetosave.persistence.specification.TagStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notesStore: NoteStore,
    private val tagStore: TagStore,
    private val reminderStore: ReminderStore,
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
            launch {
                tagStore.observeTags().collect { tags ->
                    reduce { state.copy(tags = tags) }
                }
            }
            launch {
                combine(
                    reminderStore.observeReminders(),
                    notesStore.observeNotes()
                ) { reminders, notes ->
                    val noteMap = notes.associateBy { it.id }
                    reminders.map { reminder -> ReminderWithNote(reminder, noteMap[reminder.noteId]) }
                }.collect { items ->
                    reduce { state.copy(reminders = items) }
                }
            }
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.AddEditNoteScreen ->
                intent {
                    postSideEffect(NavigateToAddEditNoteScreen(event.note))
                }

            is NotesEvent.DeleteNote ->
                intent {
                    if (event.note.id == null) return@intent

                    notesStore.deleteNote(event.note.id)

                    reduce {
                        state.copy(recentDeletedNote = event.note)
                    }

                    postSideEffect(ShowSnackbar("Note Deleted!", "Undo"))
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
                        state.copy(isOrderSectionVisible = !state.isOrderSectionVisible)
                    }
                }

            NotesEvent.ToggleDrawer ->
                intent {
                    reduce { state.copy(isDrawerOpen = !state.isDrawerOpen) }
                }

            is NotesEvent.SelectTag ->
                intent {
                    reduce { state.copy(activeTagFilter = event.tagId, isDrawerOpen = false) }
                }

            is NotesEvent.DeleteReminder ->
                intent {
                        reminderStore.delete(event.id)
                }

            NotesEvent.OpenRemindersScreen ->
                intent {
                    postSideEffect(NavigateToRemindersScreen)
                    reduce { state.copy(isDrawerOpen = false) }
                }

            is NotesEvent.OpenTagsScreen ->
                intent {
                    postSideEffect(NavigateToTagsScreen(alertOpened = event.openAlert))
                    reduce { state.copy(isDrawerOpen = false) }
                }

            NotesEvent.CloseDrawer ->
                intent {
                    reduce { state.copy(isDrawerOpen = false) }
                }

        }
    }
}
