package com.jfranco.sharetosave.features.posts.addEdit

import androidx.compose.ui.focus.FocusState
import com.jfranco.sharetosave.domain.Note

sealed class AddEditNoteEvent {
    sealed class ChangeFocus : AddEditNoteEvent()
    data class ChangeTitleFocus(val focusState: FocusState) : ChangeFocus()
    data class ChangeContentFocus(val focusState: FocusState) : ChangeFocus()
    data class ChangeColor(val color: Int) : AddEditNoteEvent()
    data class SaveNote(val note: Note) : AddEditNoteEvent()
    data class ToggleTag(val tagId: Long) : AddEditNoteEvent()
    data object ToggleTagPanel : AddEditNoteEvent()
}