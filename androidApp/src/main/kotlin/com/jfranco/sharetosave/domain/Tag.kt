package com.jfranco.sharetosave.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tag(
    val id: Long?,
    val name: String,
    val color: Int,
) : Parcelable
