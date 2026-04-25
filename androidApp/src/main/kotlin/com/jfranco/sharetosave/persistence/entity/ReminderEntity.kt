package com.jfranco.sharetosave.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.jfranco.sharetosave.domain.Reminder
import com.jfranco.sharetosave.domain.ReminderType
import java.time.LocalDateTime

@Entity(
    tableName = "reminder",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "note_id", index = true) val noteId: Long,
    @ColumnInfo(name = "trigger_at") val triggerAt: LocalDateTime,
    @ColumnInfo(name = "type") val type: String,
)

fun ReminderEntity.toDomain() = Reminder(
    id = this.id,
    noteId = this.noteId,
    triggerAt = this.triggerAt,
    type = ReminderType.valueOf(this.type),
)

fun List<ReminderEntity>.toDomains() = this.map { it.toDomain() }

fun Reminder.toEntity() = ReminderEntity(
    id = this.id ?: 0,
    noteId = this.noteId,
    triggerAt = this.triggerAt,
    type = this.type.name,
)
