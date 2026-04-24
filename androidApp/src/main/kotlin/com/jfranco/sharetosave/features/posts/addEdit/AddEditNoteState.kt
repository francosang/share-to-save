package com.jfranco.sharetosave.features.posts.addEdit

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.foundation.text.input.TextFieldState
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddEditNoteState(
    val title: NoteTextFieldState = NoteTextFieldState(
        hint = "Enter title..."
    ),
    val content: NoteTextFieldState = NoteTextFieldState(
        hint = "Enter some content..."
    ),
    val attachmentPath: String? = null,
    val attachmentMimeType: String? = null,
    val color: Int = -1,
    val noteId: Long? = null,
    val isNoteSaved: Boolean = false,
    val saveEnabled: Boolean = false
) : Parcelable

@Parcelize
data class NoteTextFieldState(
    val state: TextFieldState = TextFieldState(""),
    val hint: String = "",
    val isHintVisible: Boolean = true
) : Parcelable {
    private companion object : Parceler<NoteTextFieldState> {
        override fun NoteTextFieldState.write(
            parcel: Parcel,
            flags: Int
        ) {
            parcel.writeString(state.text.toString())
            parcel.writeString(hint)
            parcel.writeInt(if (isHintVisible) 1 else 0)
        }

        override fun create(parcel: Parcel): NoteTextFieldState {
            return NoteTextFieldState(
                state = TextFieldState(parcel.readString() ?: ""),
                hint = parcel.readString() ?: "",
                isHintVisible = parcel.readInt() != 0
            )
        }
    }
}