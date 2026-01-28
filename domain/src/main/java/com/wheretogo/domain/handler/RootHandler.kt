package com.wheretogo.domain.handler

interface RootHandler {
    suspend fun handle(event: RootEvent, data: Any? = null)
    suspend fun handle(error: Throwable): Throwable
}