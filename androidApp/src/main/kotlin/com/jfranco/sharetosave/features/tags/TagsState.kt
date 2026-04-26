package com.jfranco.sharetosave.features.tags

import android.os.Parcelable
import com.jfranco.sharetosave.domain.Tag
import kotlinx.parcelize.Parcelize

@Parcelize
data class TagsState(
    val tags: List<Tag> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingTag: Tag? = null,
) : Parcelable
