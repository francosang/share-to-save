package com.jfranco.sharetosave.features.posts.addEdit

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jfranco.sharetosave.domain.Note
import com.jfranco.sharetosave.features.posts.shared.SharedDataRepository
import com.jfranco.sharetosave.persistence.specification.NoteStore
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val noteStore: NoteStore,
    private val sharedDataRepository: SharedDataRepository
) : ViewModel(), ContainerHost<AddEditNoteState, AddEditNoteSideEffect> {

    private val existingNote: Note? = AddEditScreenDestination.argsFrom(savedStateHandle).note

    override val container = container<AddEditNoteState, AddEditNoteSideEffect>(
        initialState = run {
            val note = existingNote ?: run {
                val sharedText = sharedDataRepository.sharedText.value
                val sharedImageUri = sharedDataRepository.sharedImageUri.value
                if (sharedText != null || sharedImageUri != null) {
                    Note(
                        id = null,
                        title = sharedText,
                        content = null,
                        image = sharedImageUri.toString(),
                        created = LocalDateTime.now(),
                        edited = null,
                        color = 0
                    )
                } else {
                    null
                }
            }

            AddEditNoteState(
                title = NoteTextFieldState(
                    state = TextFieldState(note?.title.orEmpty()),
                    hint = "Enter title...",
                    isHintVisible = note?.title.isNullOrBlank()
                ),
                content = NoteTextFieldState(
                    state = TextFieldState(note?.content.orEmpty()),
                    hint = "Enter some content...",
                    isHintVisible = note?.content.isNullOrBlank()
                ),
                image = note?.image,
                color = note?.color ?: -1,
                saveEnabled = !note?.title.isNullOrBlank() || !note?.content.isNullOrBlank()
                    || note?.image != null
            )
        },
        savedStateHandle = savedStateHandle
    ) {
        Log.i("AddEditNoteViewModel", "Container initialized")
        sharedDataRepository.setSharedText(null)
        sharedDataRepository.setSharedImageUri(null)

        // Used to launch in parallel coroutines listening to the text fields changes
        coroutineScope {
            Log.i("AddEditNoteViewModel", "Coroutine scope launched")
            launch {
                combine(
                    snapshotFlow { state.title.state.text },
                    snapshotFlow { state.content.state.text }
                ) { title, content ->
                    title to content
                }.collectLatest { (title, content) ->
                    reduce {
                        state.copy(
                            saveEnabled = title.isNotBlank() or content.isNotBlank()
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditNoteEvent) {
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
                            isHintVisible = !event.focusState.isFocused && state.content.state.text.isBlank()
                        )
                        state.copy(content = content)
                    }
                }

            is AddEditNoteEvent.ChangeTitleFocus ->
                intent {
                    reduce {
                        val title = state.title.copy(
                            isHintVisible = !event.focusState.isFocused && state.title.state.text.isBlank()
                        )
                        state.copy(title = title)
                    }
                }

            is AddEditNoteEvent.SaveNote ->
                intent {
                    Log.i("AddEditNoteViewModel", "SaveNote: $state")

                    val note = noteStore.save(event.note)

                    Log.i("AddEditNoteViewModel", "SaveNote: $note")

                    postSideEffect(AddEditNoteSideEffect.NavigateBackWithResult(note))
                }
        }
    }
}