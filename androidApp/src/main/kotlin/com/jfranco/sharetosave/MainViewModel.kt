package com.jfranco.sharetosave

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfranco.sharetosave.features.posts.shared.SharedDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sharedDataRepository: SharedDataRepository
) : ViewModel() { // Or your Orbit ViewModel

    val imageToDisplay: StateFlow<Uri?> = sharedDataRepository.sharedImageUri
    val sharedText: StateFlow<String?> = sharedDataRepository.sharedText

    // If using Orbit, you might post a side effect or update state
    // based on sharedDataRepository.sharedImageUri changes.
    // For example, in an init block or a specific intent handler:
//    init {
//        viewModelScope.launch {
//            imageToDisplay.collect { uri ->
//                if (uri != null) {
//                    // container.intent { reduce { state.copy(sharedImage = uri) } }
//                    // container.sideEffect { PostSideEffect.NavigateToEditScreen(uri) }
//                    // Reset it in the repository if it's a one-time event
//                    // sharedDataRepository.setSharedImageUri(null)
//
//                    Log.d("MyApp", "MainViewModel: init ------------------: $uri")
//                }
//            }
//        }
//    }
}