package com.wheretogo.presentation.composable.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.EventResult

@Composable
fun AppEventSendEffect(onSend: (AppEvent)-> Unit){
    LaunchedEffect(Unit) {
        EventBus.sendFlow.collect {
            onSend(it)
        }
    }
}

@Composable
fun AppEventReceiveEffect(onReceive: (EventResult)-> Unit){
    LaunchedEffect(Unit) {
        EventBus.receiveFlow.collect {
            onReceive(it)
        }
    }
}