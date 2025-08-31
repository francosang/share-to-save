package com.jfranco.sharetosave.common.handler

abstract class ActionHandler<A, S> {

    operator fun invoke(action: A, state: S): S = handle(action, state)

    protected abstract fun handle(action: A, state: S): S

}