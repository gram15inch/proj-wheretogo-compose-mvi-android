package com.wheretogo.domain.model.user

import com.wheretogo.domain.DOMAIN_EMPTY

data class ProfilePrivate(
    val mail: String = "",
    val authCompany: String = DOMAIN_EMPTY,
    val lastVisited: Long = 0L,
    val accountCreation: Long = 0L,
    val isAdRemove: Boolean = false
)
