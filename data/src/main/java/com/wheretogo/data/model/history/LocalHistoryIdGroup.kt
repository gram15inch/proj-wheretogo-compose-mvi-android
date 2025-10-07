package com.wheretogo.data.model.history

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class LocalHistoryIdGroup(
    val groupById: Map<String, HashSet<String>> = emptyMap(),
)