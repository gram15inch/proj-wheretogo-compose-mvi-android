package com.wheretogo.domain.handler


interface DriveHandler {
    suspend fun handle(event: DriveEvent)
    suspend fun handle(error: Throwable): Throwable
}