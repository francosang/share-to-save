package com.jfranco.sharetosave.features.posts.addEdit

import android.content.Context
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jfranco.sharetosave.di.IoDispatcher
import com.jfranco.sharetosave.domain.FileStorageHelper
import com.jfranco.sharetosave.domain.Note
import com.jfranco.sharetosave.persistence.specification.NoteStore
import com.jfranco.sharetosave.persistence.specification.TagStore
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteStore: NoteStore,
    private val tagStore: TagStore,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), ContainerHost<AddEditNoteState, AddEditNoteSideEffect> {

    private val arg: AddEditScreenDestinationArgs = AddEditScreenDestination.argsFrom(savedStateHandle)

    private val existingNote = arg.note
    private val sharedFileUri = arg.fileUri
    private val sharedMimeType = arg.mimeType
    private val sharedText = arg.text
    private val isFromShare = arg.fromShare

    override val container = container<AddEditNoteState, AddEditNoteSideEffect>(
        initialState = run {
            val note = existingNote

            val sharedContent = if (isFromShare) sharedText.orEmpty() else ""
            AddEditNoteState(
                title = NoteTextFieldState(
                    state = TextFieldState(note?.title.orEmpty()),
                    hint = "Enter title...",
                    isHintVisible = note?.title.isNullOrBlank()
                ),
                content = NoteTextFieldState(
                    state = TextFieldState(note?.content ?: sharedContent),
                    hint = "Enter some content...",
                    isHintVisible = (note?.content ?: sharedContent).isBlank()
                ),
                attachmentPath = note?.attachmentPath,
                attachmentMimeType = note?.attachmentMimeType ?: if (isFromShare) sharedMimeType else null,
                color = note?.color ?: 0,
                noteId = note?.id,
                saveEnabled = !note?.title.isNullOrBlank() || !note?.content.isNullOrBlank()
                    || note?.attachmentPath != null,
                isFromShare = isFromShare,
                isTagPanelExpanded = isFromShare,
            )
        },
        savedStateHandle = savedStateHandle
    ) {
        Log.i("AddEditNoteViewModel", "Container initialized")

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

            launch {
                tagStore.observeTags().collect { tags ->
                    reduce { state.copy(tags = tags) }
                }
            }

            existingNote?.id?.let { noteId ->
                launch {
                    val selectedTags = withContext(ioDispatcher) {
                        tagStore.getTagsForNote(noteId)
                    }
                    reduce { state.copy(selectedTagIds = selectedTags.mapNotNull { it.id }) }
                }
            }

            if (isFromShare) {
                launch {
                    if (sharedFileUri != null) {
                        reduce { state.copy(isAttachmentLoading = true) }
                    }

                    val absolutePath = sharedFileUri?.let { uri ->
                        withContext(ioDispatcher) {
                            FileStorageHelper.saveSharedFileInternal(context, uri, sharedMimeType)
                        }
                    }

                    val note = Note(
                        id = null,
                        title = null,
                        content = sharedText,
                        attachmentPath = absolutePath,
                        attachmentMimeType = sharedMimeType,
                        created = LocalDateTime.now(),
                        edited = null,
                        color = 0
                    )

                    val savedNote = noteStore.save(note)

                    reduce {
                        state.copy(
                            attachmentPath = savedNote.attachmentPath,
                            attachmentMimeType = savedNote.attachmentMimeType,
                            noteId = savedNote.id,
                            isNoteSaved = true,
                            isAttachmentLoading = false
                        )
                    }

                    // Flush tags toggled before noteId was available (V12)
                    val pendingTagIds = state.selectedTagIds
                    if (pendingTagIds.isNotEmpty()) {
                        withContext(ioDispatcher) {
                            noteStore.setNoteTags(requireNotNull(savedNote.id) { "insert returned null id" }, pendingTagIds)
                        }
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

            is AddEditNoteEvent.ToggleTag ->
                intent {
                    val tagId = event.tagId
                    val updated = if (tagId in state.selectedTagIds) {
                        state.selectedTagIds - tagId
                    } else {
                        state.selectedTagIds + tagId
                    }
                    // V12: persist updated list before reducing so DB and state stay in sync
                    if (state.isFromShare) {
                        state.noteId?.let { noteId ->
                            withContext(ioDispatcher) {
                                noteStore.setNoteTags(noteId, updated)
                            }
                        }
                    }
                    reduce { state.copy(selectedTagIds = updated) }
                }

            is AddEditNoteEvent.ToggleTagPanel ->
                intent {
                    reduce { state.copy(isTagPanelExpanded = !state.isTagPanelExpanded) }
                }
        }
    }
}