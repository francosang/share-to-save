package com.jfranco.sharetosave.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jfranco.sharetosave.domain.Note
import java.time.LocalDateTime

@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "image") val image: String?,
    @ColumnInfo(name = "created") val created: LocalDateTime,
    @ColumnInfo(name = "edited") val edited: LocalDateTime?,
    @ColumnInfo(name = "color") val color: Int,
)

fun List<NoteEntity>.toDomains() = this.map { it.toDomain() }
fun NoteEntity.toDomain() = Note(
    id = this.id,
    title = this.title,
    content = this.content,
    image = this.image,
    created = this.created,
    edited = this.edited,
    color = this.color
)

fun Note.toEntity() = NoteEntity(
    id = this.id ?: 0, // in our Room setup, 0 means not saved
    title = this.title,
    content = this.content,
    image = this.image,
    created = this.created,
    edited = this.edited,
    color = this.color
)
