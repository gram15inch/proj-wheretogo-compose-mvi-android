package com.wheretogo.presentation.model

data class EventMsg(
    val strRes: Int,
    val arg: String? = null,
    val labelRes: Int? = null,
    val uri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)