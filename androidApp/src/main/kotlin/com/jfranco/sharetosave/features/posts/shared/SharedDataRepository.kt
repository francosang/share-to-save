package com.jfranco.sharetosave.features.posts.shared

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedDataRepository @Inject constructor() {
    private val _sharedImageUri = MutableStateFlow<Uri?>(null)
    val sharedImageUri: StateFlow<Uri?> = _sharedImageUri.asStateFlow()

    private val _sharedText = MutableStateFlow<String?>(null)
    val sharedText: StateFlow<String?> = _sharedText.asStateFlow()

    fun setSharedImageUri(uri: Uri?) {
        Log.d("MyApp", "SharedDataRepository: setSharedImageUri -> $uri")
        _sharedImageUri.value = uri
    }

    fun setSharedText(text: String?) {
        Log.d("MyApp", "SharedDataRepository: setSharedText -> $text")
        _sharedText.value = text
    }
}