package com.jfranco.sharetosave.features.tags

import com.jfranco.sharetosave.domain.Tag

sealed class TagsEvent {
    data class AddTag(val name: String, val color: Int) : TagsEvent()
    data class DeleteTag(val id: Long) : TagsEvent()
    data class EditTag(val tag: Tag) : TagsEvent()
    object ShowAddDialog : TagsEvent()
    data class ShowEditDialog(val tag: Tag) : TagsEvent()
    object DismissDialog : TagsEvent()
}
