package com.jfranco.sharetosave.features.posts.addEdit

import androidx.compose.ui.focus.FocusState

sealed class AddEditNoteEvent {
    data class ChangeTitleFocus(val focusState: FocusState) : AddEditNoteEvent()
    data class ChangeContentFocus(val focusState: FocusState) : AddEditNoteEvent()
    data class ChangeColor(val color: Int) : AddEditNoteEvent()
    object SaveNote : AddEditNoteEvent()
}