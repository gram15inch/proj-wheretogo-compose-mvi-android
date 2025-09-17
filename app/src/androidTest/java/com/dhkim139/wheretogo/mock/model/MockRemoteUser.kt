package com.dhkim139.wheretogo.mock.model

import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile

data class MockRemoteUser(
    val token: String = "",
    val profile: Profile = Profile(),
    val history: History = History()
)