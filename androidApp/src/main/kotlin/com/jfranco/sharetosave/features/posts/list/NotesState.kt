package com.jfranco.sharetosave.features.posts.list

import android.os.Parcelable
import com.jfranco.sharetosave.common.theme.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
    val id: Int? = null
) : Parcelable {
    companion object {
        val noteColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
    }
}

@Parcelize
data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val recentDeletedNote: Note? = null
) : Parcelable
