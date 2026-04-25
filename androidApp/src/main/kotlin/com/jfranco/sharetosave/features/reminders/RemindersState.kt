package com.jfranco.sharetosave.features.reminders

import android.os.Parcelable
import com.jfranco.sharetosave.domain.ReminderWithNote
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemindersState(
    val reminders: List<ReminderWithNote> = emptyList()
) : Parcelable
