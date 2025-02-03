package com.wheretogo.domain.model.user

import com.wheretogo.domain.DOMAIN_EMPTY

data class Profile(
    val uid: String = DOMAIN_EMPTY,
    val name: String ="",
    val hashMail: String = "",
    val private: ProfilePrivate = ProfilePrivate()
)
