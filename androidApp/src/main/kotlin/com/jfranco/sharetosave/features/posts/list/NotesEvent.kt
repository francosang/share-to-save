package com.jfranco.sharetosave.features.posts.list

import android.os.Parcelable
import com.jfranco.sharetosave.domain.Note
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class OrderType : Parcelable {
    object Ascending : OrderType()
    object Descending : OrderType()
}

@Parcelize
sealed class NoteOrder(open val orderType: OrderType) : Parcelable {

    class Title(override val orderType: OrderType) : NoteOrder(orderType)
    class Date(override val orderType: OrderType) : NoteOrder(orderType)
    class Color(override val orderType: OrderType) : NoteOrder(orderType)

    //This function would be accessible in UI
    fun copyNoteHelper(orderType: OrderType): NoteOrder {
        return when (this) {
            is Title -> Title(orderType)
            is Date -> Date(orderType)
            is Color -> Color(orderType)
        }
    }
}

sealed class NotesEvent {
    data class Order(val noteOrder: NoteOrder) : NotesEvent()
    data class DeleteNote(val note: Note) : NotesEvent()
    object RestoreNote : NotesEvent()
    object ToggleOrderSection : NotesEvent()
    data class AddEditNoteScreen(val note: Note?) : NotesEvent()
}