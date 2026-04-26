package com.jfranco.sharetosave.features.posts.list

import com.jfranco.sharetosave.domain.Note

sealed class NotesSideEffect {
    data class NavigateToAddEditNoteScreen(val note: Note?) : NotesSideEffect()
    data class ShowSnackbar(val message: String, val actionLabel: String) : NotesSideEffect()
    object NavigateToRemindersScreen : NotesSideEffect()
    data class NavigateToTagsScreen(val alertOpened: Boolean) : NotesSideEffect()
}
