package com.dhkim139.wheretogo.feature

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.turbineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class FlowAssertions<S, E>(
    val state: ReceiveTurbine<S>,
    val event: ReceiveTurbine<E>,
)

suspend fun <S, E> assertFlows(
    stateFlow: StateFlow<S>,
    eventFlow: SharedFlow<E>,
    skipInitialState: Boolean = true,
    block: suspend FlowAssertions<S, E>.() -> Unit,
) {
    turbineScope {
        val assertions = FlowAssertions(
            stateFlow.testIn(this),
            eventFlow.testIn(this),
        )
        try {
            if (skipInitialState) assertions.state.awaitItem()
            assertions.block()
        } finally {
            assertions.state.cancel()
            assertions.event.cancel()
        }
    }
}