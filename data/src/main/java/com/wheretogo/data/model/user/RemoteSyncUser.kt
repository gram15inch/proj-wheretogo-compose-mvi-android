package com.wheretogo.data.model.user

import com.squareup.moshi.Json
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper

data class RemoteSyncUser(
    @Json(name = "publicUser")  val public: RemoteProfilePublic,
    @Json(name = "privateUser") val private: RemoteProfilePrivate,
    @Json(name = "history")     val history: List<RemoteHistoryGroupWrapper>
)