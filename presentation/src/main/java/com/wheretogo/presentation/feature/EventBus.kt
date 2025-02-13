package com.wheretogo.presentation.feature

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.model.EventMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object EventBus {
    private val _eventFlow = MutableSharedFlow<Pair<AppEvent, EventMsg>>()
    val eventFlow = _eventFlow.asSharedFlow()

    suspend fun sendMsg(msg: EventMsg) {
        _eventFlow.emit(Pair(AppEvent.SNACKMAR, msg))
    }

    suspend fun navigation(@StringRes navigation: Int) {
        _eventFlow.emit(Pair(AppEvent.NAVIGATION, EventMsg(navigation)))
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