package com.wheretogo.domain.handler

interface LoginHandler {
    suspend fun handle(event: LoginEvent, arg: String = "")
    suspend fun handle(error: Throwable): Throwable
}