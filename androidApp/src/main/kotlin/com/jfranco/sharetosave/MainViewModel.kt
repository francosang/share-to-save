package com.jfranco.sharetosave

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.jfranco.sharetosave.features.posts.shared.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedDataRepository: SharedDataRepository
) : ViewModel() {

    val imageToDisplay: StateFlow<Uri?> = sharedDataRepository.sharedImageUri
    val sharedText: StateFlow<String?> = sharedDataRepository.sharedText

    fun consumeSharedData() {
        Log.d("MyApp", "MainViewModel: consumeSharedData — clearing repository")
        sharedDataRepository.setSharedImageUri(null)
        sharedDataRepository.setSharedText(null)
    }
}