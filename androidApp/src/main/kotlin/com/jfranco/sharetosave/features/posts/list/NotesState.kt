package com.jfranco.sharetosave.features.posts.list

import android.os.Parcelable
import com.jfranco.sharetosave.domain.Note
import com.jfranco.sharetosave.domain.ReminderWithNote
import com.jfranco.sharetosave.domain.Tag
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val recentDeletedNote: Note? = null,
    val tags: List<Tag> = emptyList(),
    val activeTagFilter: Long? = null,
    val isDrawerOpen: Boolean = false,
    val reminders: List<ReminderWithNote> = emptyList(),
) : Parcelable
