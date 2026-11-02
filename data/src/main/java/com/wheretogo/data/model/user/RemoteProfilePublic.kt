package com.wheretogo.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class RemoteProfilePublic(
    val uid: String = "",
    val name: String = "",
    val hashMail: String = ""
)