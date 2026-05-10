package com.wheretogo.domain.handler


interface DriveHandler {
    suspend fun handle(event: DriveMsgEvent)
    suspend fun handle(error: Throwable): Throwable
}