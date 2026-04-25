package com.jfranco.sharetosave.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jfranco.sharetosave.domain.Tag

@Entity(tableName = "tag")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color") val color: Int,
)

fun TagEntity.toDomain() = Tag(id = this.id, name = this.name, color = this.color)
fun List<TagEntity>.toDomains() = this.map { it.toDomain() }
fun Tag.toEntity() = TagEntity(id = this.id ?: 0, name = this.name, color = this.color)
