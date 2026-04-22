package com.jfranco.sharetosave.features.posts.list

import android.os.Parcelable
import com.jfranco.sharetosave.domain.Note
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val recentDeletedNote: Note? = null
) : Parcelable
