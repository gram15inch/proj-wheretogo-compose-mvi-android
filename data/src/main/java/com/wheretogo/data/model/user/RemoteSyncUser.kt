package com.wheretogo.data.model.user

import com.squareup.moshi.Json
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper

data class RemoteSyncUser(
    val publicUser: RemoteProfilePublic = RemoteProfilePublic(),
    val privateUser: RemoteProfilePrivate = RemoteProfilePrivate(),
    val history: List<RemoteHistoryGroupWrapper> = emptyList()
)