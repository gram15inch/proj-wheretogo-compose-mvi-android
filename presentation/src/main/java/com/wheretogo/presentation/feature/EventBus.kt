package com.wheretogo.presentation.feature

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.wheretogo.domain.UseCaseFailType
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.toStringRes
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

suspend fun SnackbarHostState.shortShow(context: Context, eventMsg:EventMsg, onActionPerformed:(String)->Unit){
   withContext(Dispatchers.IO){
       val message = eventMsg.getString(context)
       val actionLabel = eventMsg.labelRes?.run{context.getString(this)}
       val job = launch {
           val result= showSnackbar(
               message = message,
               actionLabel = actionLabel
           )
           if(result == SnackbarResult.ActionPerformed&& eventMsg.uri!=null)
               onActionPerformed(eventMsg.uri)
       }

       delay(1500)
       job.cancel()
   }
}

fun EventMsg.getString(context: Context):String{
    return if (!arg.isNullOrBlank()) {
        runCatching {
            UseCaseFailType.valueOf(arg!!)
        }.fold(
            onSuccess = {
                val argStr = it.toStringRes()?.run { context.getString(this) }?:""
                context.getString(strRes, argStr)
            },
            onFailure ={context.getString(strRes, arg)}
        )
    } else {
        context.getString(strRes)
    }
}