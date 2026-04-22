package com.jfranco.sharetosave

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject


sealed class MainViewSideEffect {
    class OnDataShared(val text: String?, val image: Uri?) : MainViewSideEffect()
}

sealed class MainEvent {
    class OnDataShared(val text: String?, val image: Uri?) : MainEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(), ContainerHost<Unit, MainViewSideEffect> {

    override val container = container<Unit, MainViewSideEffect>(Unit)

    fun onEvent(event: MainEvent) {
        Log.i("MainViewModel", "event: $event")

        when (event) {
            is MainEvent.OnDataShared ->
                intent {
                    postSideEffect(MainViewSideEffect.OnDataShared(event.text, event.image))
                }
        }
    }


}