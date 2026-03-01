package com.wheretogo.presentation.feature

import kotlinx.coroutines.flow.MutableStateFlow


class LoggingStateFlow<T>(
    private val delegate: MutableStateFlow<T>,
    private val log: (StackTraceElement?, T) -> Unit
) : MutableStateFlow<T> by delegate {

    override var value: T
        get() = delegate.value
        set(newValue) {
            logCaller(newValue)
            delegate.value = newValue
        }

    override suspend fun emit(value: T) {
        logCaller(value)
        delegate.emit(value)
    }

    override fun tryEmit(value: T): Boolean {
        logCaller(value)
        return delegate.tryEmit(value)
    }

    override fun compareAndSet(expect: T, update: T): Boolean {
        logCaller(update)
        return delegate.compareAndSet(expect, update)
    }

    private fun logCaller(newValue: T) {
        val stackTrace = Thread.currentThread().stackTrace
        val caller = stackTrace.getOrNull(4)
        log(caller, newValue)
    }
}

fun <T> MutableStateFlow<T>.withLogging(
    log: (StackTraceElement?, T) -> Unit
): MutableStateFlow<T> = LoggingStateFlow(this, log)

fun StackTraceElement.shortPath(): String =
    "${fileName}:${lineNumber}.${methodName}"