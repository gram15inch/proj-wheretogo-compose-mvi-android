package com.wheretogo.presentation.model

data class EventMsg(
    val strRes: Int,
    val arg: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)