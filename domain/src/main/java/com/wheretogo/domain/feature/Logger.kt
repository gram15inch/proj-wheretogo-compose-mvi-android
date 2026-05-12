package com.wheretogo.domain.feature

import timber.log.Timber


fun whosCall(tag: String="tst_", data: Any? = "") {
    val stack = Throwable().stackTrace
        .filter {
            it.className.startsWith("com.wheretogo") && !it.className.contains("LoggingKt")
        }.joinToString("\n") { "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
    Timber.d("$tag $data \n→ ${stack}")
}