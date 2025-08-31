package com.jfranco.sharetosave.features.posts.addEdit

sealed class AddEditNoteSideEffect {
    data class ShowSnackBar(val message: String) : AddEditNoteSideEffect()
    object SaveNote : AddEditNoteSideEffect()
}