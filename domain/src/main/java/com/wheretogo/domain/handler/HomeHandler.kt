package com.wheretogo.domain.handler

interface HomeHandler {
    suspend fun handle(event: HomeEvent)
    suspend fun handle(error: Throwable): Throwable
}