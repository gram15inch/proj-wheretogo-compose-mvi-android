package com.wheretogo.data.model.user


data class RemoteProfilePrivate(
    val mail: String = "",
    val authCompany: String = "",
    val lastVisited: Long = 0L,
    val accountCreation: Long = 0L,
    val adRemove: Boolean = false,
    val admin: Boolean = false,
    val msgToken: String = ""
)
