package com.wheretogo.data.model.user

import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import kotlinx.serialization.Serializable

@Serializable
data class RemoteSyncUser(
    val publicUser: RemoteProfilePublic = RemoteProfilePublic(),
    val privateUser: RemoteProfilePrivate = RemoteProfilePrivate(),
    val history: List<RemoteHistoryGroupWrapper> = emptyList()
)