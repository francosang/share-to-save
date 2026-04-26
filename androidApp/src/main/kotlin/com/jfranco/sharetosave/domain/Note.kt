package com.jfranco.sharetosave.domain

import android.os.Parcelable
import com.jfranco.sharetosave.common.theme.BabyBlue
import com.jfranco.sharetosave.common.theme.LightGreen
import com.jfranco.sharetosave.common.theme.RedOrange
import com.jfranco.sharetosave.common.theme.RedPink
import com.jfranco.sharetosave.common.theme.Violet
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Note(
    val id: Long?,
    val title: String?,
    val content: String?,
    val attachmentPath: String?,
    val attachmentMimeType: String?,
    val created: LocalDateTime,
    val edited: LocalDateTime?,
    val color: Int,
    val tagIds: List<Long> = emptyList(),
) : Parcelable {
    companion object {
        val noteColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
    }
}
