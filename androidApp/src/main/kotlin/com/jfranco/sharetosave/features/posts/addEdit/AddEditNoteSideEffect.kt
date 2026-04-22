package com.jfranco.sharetosave.features.posts.addEdit

import com.jfranco.sharetosave.domain.Note

sealed class AddEditNoteSideEffect {
    class NavigateBackWithResult(val noteSaved: Note) : AddEditNoteSideEffect()
}