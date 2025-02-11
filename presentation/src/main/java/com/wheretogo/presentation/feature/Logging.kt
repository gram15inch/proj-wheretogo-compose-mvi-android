package com.wheretogo.presentation.feature

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow

class LoggingStateFlow<T>(private val flow: MutableStateFlow<T>, val log :(StackTraceElement?, T)->Unit) : MutableStateFlow<T> by flow {
    override var value: T
        get() = flow.value
        set(newValue) {
            logCaller(newValue, log)
            flow.value = newValue
        }

    private fun logCaller(value: T, log :(StackTraceElement?, T)->Unit) {
        val stackTrace = Thread.currentThread().stackTrace
        val caller = stackTrace.getOrNull(4)
        log(caller, value)
    }
}
fun <T> MutableStateFlow<T>.withLogging(log:(StackTraceElement?, T)->Unit): MutableStateFlow<T> {
    return LoggingStateFlow(this, log)
}

fun StackTraceElement.shortPath():String = "${fileName}:${lineNumber}.${methodName}"

fun logCurrentTrace() {
    Thread.currentThread().stackTrace.apply {
        Log.d("tst_","${this.contentToString()}")
    }
}