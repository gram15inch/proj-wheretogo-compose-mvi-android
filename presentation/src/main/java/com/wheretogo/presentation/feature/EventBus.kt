package com.wheretogo.presentation.feature

import android.content.Context
import androidx.compose.material3.SnackbarDuration
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

data class EventResult(
    val event: AppEvent,
    val isSuccess: Boolean,
    val data: Any? = null
)

object EventBus {
    private val _receiveFlow = MutableSharedFlow<EventResult>()
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
                        .filter { it.event == event }
                        .first()
                    return@withTimeoutOrNull result.isSuccess
                } ?: run {
                     _receiveFlow.emit(EventResult(event,false))
                     false
                 }
            }
            else -> true
        }
    }

    suspend fun result(event: AppEvent, bool: Boolean) {
        _receiveFlow.emit(EventResult(event, bool))
    }

    suspend fun resultPermission(event: AppEvent, result:Any) {
        _receiveFlow.emit(EventResult(event, result!=false, result))
    }
}

suspend fun SnackbarHostState.show(
    context: Context,
    eventMsg: EventMsg,
    onActionPerformed: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        val message = eventMsg.getString(context)
        val actionLabel = eventMsg.labelRes?.run { context.getString(this) }
        val duration =  when{
            eventMsg.isLongShow -> SnackbarDuration.Long
            actionLabel == null -> SnackbarDuration.Short
            else -> SnackbarDuration.Indefinite
        }
        val delay = if(eventMsg.isLongShow) 1000*10L else 1500L
        val job = launch {
            val result = showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration =  duration
            )
            if (result == SnackbarResult.ActionPerformed && eventMsg.uri != null)
                onActionPerformed(eventMsg.uri)
        }

        delay(delay)
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