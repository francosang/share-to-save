package com.jfranco.sharetosave.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

enum class ReminderType { DAY, WEEK, MONTH, YEAR, CUSTOM }

@Parcelize
data class Reminder(
    val id: Long?,
    val noteId: Long,
    val triggerAt: LocalDateTime,
    val type: ReminderType,
) : Parcelable
