package com.wheretogo.presentation.feature

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class LoggingStateFlow<T>(
    initialValue: T,
    private val name: String = "StateFlow",
    val log:(T)->String = {_-> ""},
) : MutableStateFlow<T> {

    private val _flow = MutableStateFlow(initialValue)

    override var value: T
        get() = _flow.value
        set(value) {
            logCaller(value)
            _flow.value = value
        }

    override val replayCache get() = _flow.replayCache
    override val subscriptionCount get() = _flow.subscriptionCount

    override suspend fun collect(collector: FlowCollector<T>) =
        _flow.collect(collector)

    override fun tryEmit(value: T): Boolean {
        return _flow.tryEmit(value)
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {

    }

    override suspend fun emit(value: T) {
        _flow.emit(value)
    }

    override fun compareAndSet(expect: T, update: T) =
        _flow.compareAndSet(expect, update)

    fun update(function: (T) -> T) {
        while (true) {
            val prev = value
            val next = function(prev)
            if (_flow.compareAndSet(prev, next)) {
                logCaller(next)
                return
            }
        }
    }

    private fun logCaller(value: T) {
        val stack = Throwable().stackTrace
            .filter {
                it.className.startsWith("com.wheretogo") && !it.className.contains("LoggingStateFlow")
            }.joinToString("\n") { "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
        Timber.d("update \n[$name] → [${log(value)}] ${stack}")
    }
}