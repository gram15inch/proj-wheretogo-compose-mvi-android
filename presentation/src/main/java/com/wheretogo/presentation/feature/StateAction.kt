package com.wheretogo.presentation.feature

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

suspend fun <Data, State> MutableStateFlow<State>.executeAction(
    loading: suspend (State, Boolean) -> State,
    action: suspend () -> Result<Data>,
    onSuccess: suspend (Data) -> Any = {},
    onFailure: suspend (Throwable) -> Unit
) {
    update { loading(it, true) }
    withContext(Dispatchers.IO) { action() }
        .onSuccess { onSuccess(it) }
        .onFailure {
            onFailure(it)
        }
    update { loading(it, false) }
}

suspend fun <Data, State> MutableStateFlow<State>.executeActionWithUpdateUi(
    loading: suspend (State, Boolean) -> State,
    action: suspend () -> Result<Data>,
    onBeforeActionUi: (suspend (State) -> State)? = null,
    onSuccessUi: (suspend (State) -> State)? = null,
    onSuccess: suspend (Data) -> Any = {},
    onFailureUi: (suspend (State) -> State)? = null,
    onFailure: suspend (Throwable) -> Unit = { },
    isSuccessUiUpdateFirst: Boolean = true
) {
    suspend fun updateWithLoading(
        uiUpdate: (suspend (State) -> State)?,
        isLoading: Boolean = false
    ) {
        update { old ->
            val new = uiUpdate?.invoke(old)
            if (new == null)
                loading(old, isLoading)
            else
                loading(new, isLoading)
        }
    }

    updateWithLoading(onBeforeActionUi, true)
    withContext(Dispatchers.IO) { action() }
        .onSuccess {
            if (isSuccessUiUpdateFirst)
                updateWithLoading(onSuccessUi)
            onSuccess(it)
            if (!isSuccessUiUpdateFirst)
                updateWithLoading(onSuccessUi)
        }.onFailure {
            updateWithLoading(onFailureUi)
            onFailure(it)
        }
}