package com.jfranco.sharetosave.features.posts.list

import com.jfranco.sharetosave.domain.Note

sealed class NotesSideEffect {
    data class NavigateToAddEditNoteScreen(val note: Note?) : NotesSideEffect()
}
