package com.wheretogo.data.model.user

data class LocalProfile(
    val uid: String = "",
    val name: String = "",
    val hashMail: String = "",
    val private: LocalProfilePrivate = LocalProfilePrivate()
)
