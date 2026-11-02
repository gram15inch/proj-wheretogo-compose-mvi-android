package com.wheretogo.data.feature

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TraceExport {
    fun export(e: Exception, context: Context){
        val text = buildString {
            var cur: Throwable? = e
            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            append("===========================").append('\n')
            append("$time").append('\n')
            append("===========================").append('\n')
            while (cur != null) {
                append(cur::class.java.name).append('\n')
                append("message: ").append(cur.message).append("\n\n")
                cur.stackTrace.forEach { append("\tat ").append(it).append('\n') }
                append('\n')
                cur = cur.cause
            }
        }
        File(context.getExternalFilesDir(null), "trace_${System.currentTimeMillis()}.txt").writeText(text)
    }
}