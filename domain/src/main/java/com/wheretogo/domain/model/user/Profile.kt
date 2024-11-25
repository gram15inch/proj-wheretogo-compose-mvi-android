package com.wheretogo.domain.model.user

import com.wheretogo.domain.EMPTY

data class Profile(
    val uid: String = EMPTY,
    val mail: String = "",
    val name: String = "",
    val authCompany: String = EMPTY,
    val lastVisited: Long = 0L,
    val accountCreation: Long = 0L,
    val isAdRemove: Boolean = false
)
