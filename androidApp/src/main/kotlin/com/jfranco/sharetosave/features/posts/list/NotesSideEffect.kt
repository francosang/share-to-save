package com.jfranco.sharetosave.features.posts.list

sealed class NotesSideEffect {
    data class NavigateToAddEditNoteScreen(val note: Note?) : NotesSideEffect()
}
