package com.wheretogo.domain.model.user

data class Profile(
    val id: String = "",
    val name: String = "",
    val lastVisitedDate: Long = 0L,
    val accountCreationDate: Long = 0L,
    val isAdRemove: Boolean = false,
)
