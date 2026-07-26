package com.dhkim139.core.ui.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

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