package com.wheretogo.presentation.feature

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.model.EventMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

object EventBus {
    private val _eventFlow = MutableSharedFlow<AppEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    val _resultFlow = MutableSharedFlow<Pair<String, Boolean>>()
    val resultFlow = _resultFlow.asSharedFlow()

    suspend fun send(event: AppEvent): Boolean {
        _eventFlow.emit(event)
        if (event is AppEvent.Permission) {
            return withTimeoutOrNull(10000L) {
                val result = resultFlow
                    .filter { it.first == event.permission.name }
                    .first()
                return@withTimeoutOrNull result.second
            } ?: false
        }
        return true
    }

    suspend fun permissionResult(permission: String, bool: Boolean) {
        _resultFlow.emit(Pair(permission, bool))
    }
}

fun SnackbarHostState.shortShow(msg:String){
    CoroutineScope(Dispatchers.IO).launch {
        val job = launch { showSnackbar(msg) }
        delay(1500)
        job.cancel()
    }
}

fun EventMsg.getString(context: Context):String{
    return if (arg != null) {
        context.getString(strRes, arg)
    } else {
        context.getString(strRes)
    }
}