package com.wheretogo.data.model.payload

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PublicPayload(
    val packageName: String,
    val sha256: String,
    val expireAt: Long
)

