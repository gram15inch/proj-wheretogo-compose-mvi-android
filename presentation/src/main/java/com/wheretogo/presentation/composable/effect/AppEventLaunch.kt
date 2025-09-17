package com.wheretogo.presentation.composable.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.feature.EventBus

@Composable
fun AppEventSendEffect(onSend: (AppEvent)-> Unit){
    LaunchedEffect(Unit) {
        EventBus.sendFlow.collect {
            onSend(it)
        }
    }
}

@Composable
fun AppEventReceiveEffect(onReceive: (AppEvent, Boolean)-> Unit){
    LaunchedEffect(Unit) {
        EventBus.receiveFlow.collect {(event,bool)->
            onReceive(event, bool)
        }
    }
}