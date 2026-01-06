package com.wheretogo.domain.handler

interface ErrorHandler {
    suspend fun handle(error: Throwable): Throwable
}