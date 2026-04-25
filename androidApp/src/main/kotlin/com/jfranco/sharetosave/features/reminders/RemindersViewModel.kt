package com.jfranco.sharetosave.features.reminders

import androidx.lifecycle.ViewModel
import com.jfranco.sharetosave.di.IoDispatcher
import com.jfranco.sharetosave.domain.ReminderWithNote
import com.jfranco.sharetosave.persistence.specification.NoteStore
import com.jfranco.sharetosave.persistence.specification.ReminderStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val reminderStore: ReminderStore,
    private val noteStore: NoteStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), ContainerHost<RemindersState, Nothing> {

    override val container = container<RemindersState, Nothing>(
        initialState = RemindersState(),
    ) {
        coroutineScope {
            launch {
                combine(
                    reminderStore.observeReminders(),
                    noteStore.observeNotes()
                ) { reminders, notes ->
                    val noteMap = notes.associateBy { it.id }
                    reminders.map { reminder ->
                        ReminderWithNote(reminder, noteMap[reminder.noteId])
                    }
                }.collect { items ->
                    reduce { state.copy(reminders = items) }
                }
            }
        }
    }

    fun onEvent(event: RemindersEvent) {
        when (event) {
            is RemindersEvent.DeleteReminder ->
                intent {
                    withContext(ioDispatcher) {
                        reminderStore.delete(event.id)
                    }
                }
        }
    }
}
