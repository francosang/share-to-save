package com.jfranco.sharetosave.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.util.UUID

object FileStorageHelper {

    fun saveSharedFileInternal(context: Context, uri: Uri, mimeType: String?): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val subdir = when {
                mimeType?.startsWith("image/") == true -> "images"
                mimeType?.startsWith("video/") == true -> "videos"
                mimeType?.startsWith("audio/") == true -> "audio"
                else -> "files"
            }

            val directory = File(context.filesDir, "shared_files/$subdir")
            if (!directory.exists()) directory.mkdirs()

            val ext = mimeType?.let {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
            } ?: uri.lastPathSegment?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() }
            val fileName = if (ext != null) "${UUID.randomUUID()}.$ext" else "${UUID.randomUUID()}"
            val destinationFile = File(directory, fileName)

            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e("FileStorageHelper", "Error saving shared file", e)
            null
        }
    }
}
