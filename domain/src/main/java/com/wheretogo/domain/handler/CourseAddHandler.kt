package com.wheretogo.domain.handler


interface CourseAddHandler {
    suspend fun handle(event: CourseAddEvent)
    suspend fun handle(error: Throwable): Throwable
}