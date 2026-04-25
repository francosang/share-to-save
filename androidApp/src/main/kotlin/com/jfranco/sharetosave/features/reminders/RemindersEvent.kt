package com.jfranco.sharetosave.features.reminders

sealed class RemindersEvent {
    data class DeleteReminder(val id: Long) : RemindersEvent()
}
