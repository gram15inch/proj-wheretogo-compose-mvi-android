package com.wheretogo.domain.handler

interface RootHandler {
    suspend fun handle(event: RootEvent)
    suspend fun handle(error: Throwable): Throwable
}