package com.wheretogo.data.model.user

data class LocalProfilePrivate(
    val mail: String = "",
    val authCompany: String = "",
    val lastVisited: Long = 0L,
    val accountCreation: Long = 0L,
    val isAdRemove: Boolean = false,
    val reportedCount: Int = 0,
    val isAdmin: Boolean = false
)
