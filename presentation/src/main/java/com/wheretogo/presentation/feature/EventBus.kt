package com.wheretogo.presentation.feature

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.model.EventMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

object EventBus {
    private val _receiveFlow = MutableSharedFlow<Pair<AppEvent, Boolean>>()
    val receiveFlow = _receiveFlow.asSharedFlow()
    private val _sendFlow = MutableSharedFlow<AppEvent>()
    val sendFlow = _sendFlow.asSharedFlow()


    suspend fun send(event: AppEvent) {
        _sendFlow.emit(event)
    }

    suspend fun sendWithResult(event: AppEvent): Boolean {
        _sendFlow.emit(event)
        return when(event){
            is AppEvent.Permission ->{
                 withTimeoutOrNull(10 * 1000L) {
                    val result = receiveFlow
                        .filter { it.first == event }
                        .first()
                    return@withTimeoutOrNull result.second
                } ?: run {
                     _receiveFlow.emit(Pair(event, false))
                     false
                 }
            }
            else -> true
        }
    }

    suspend fun result(event: AppEvent, bool: Boolean) {
        _receiveFlow.emit(Pair(event, bool))
    }
}

suspend fun SnackbarHostState.shortShow(
    context: Context,
    eventMsg: EventMsg,
    onActionPerformed: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        val message = eventMsg.getString(context)
        val actionLabel = eventMsg.labelRes?.run { context.getString(this) }
        val job = launch {
            val result = showSnackbar(
                message = message,
                actionLabel = actionLabel
            )
            if (result == SnackbarResult.ActionPerformed && eventMsg.uri != null)
                onActionPerformed(eventMsg.uri)
        }

        delay(1500)
        job.cancel()
    }
}

fun EventMsg.getString(context: Context): String {
    return if (!arg.isNullOrBlank()) {
        context.getString(strRes, arg)
    } else {
        context.getString(strRes)
    }
}