package com.wheretogo.domain.model.user

import com.wheretogo.domain.DOMAIN_EMPTY

data class Profile(
    val uid: String = DOMAIN_EMPTY,
    val mail: String = "",
    val name: String = "",
    val authCompany: String = DOMAIN_EMPTY,
    val lastVisited: Long = 0L,
    val accountCreation: Long = 0L,
    val likeGroup: List<String> = emptyList(),
    val bookMarkGroup: List<String> = emptyList(),
    val isAdRemove: Boolean = false
)
