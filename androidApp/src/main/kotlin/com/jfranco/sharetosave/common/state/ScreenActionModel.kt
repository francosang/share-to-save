package com.jfranco.sharetosave.common.state

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.util.*

sealed class Result<out V, out E> {
    data class Ok<V>(val value: V) : Result<V, Nothing>()
    data class Err<V, E>(val previous: V, val value: E, val uuid: UUID) : Result<V, E>()
}

abstract class ScreenActionModel<A, S>(initialAction: A, initialState: S) : ScreenModel {

    private var currentState: S = initialState
    private val actions = MutableStateFlow(initialAction)

    val state: StateFlow<Result<S, Throwable>> = actions.map { action ->
        handleAction(action, currentState).apply {
            currentState = when (this) {
                is Result.Ok -> this.value
                is Result.Err -> this.previous
            }
        }
    }.stateIn(screenModelScope, SharingStarted.Lazily, Result.Ok(initialState))

    fun action(params: A) {
        actions.tryEmit(params)
    }

    private suspend fun handleAction(action: A, currentState: S): Result<S, Throwable> {
        return withContext(Dispatchers.IO) {
            try {
                Result.Ok(handlers(action, currentState))
            } catch (e: Throwable) {
                Result.Err(currentState, e, UUID.randomUUID())
            }
        }
    }

    protected abstract fun handlers(action: A, currentState: S): S

}