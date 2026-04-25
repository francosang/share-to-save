package com.jfranco.sharetosave.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReminderWithNote(val reminder: Reminder, val note: Note?) : Parcelable
