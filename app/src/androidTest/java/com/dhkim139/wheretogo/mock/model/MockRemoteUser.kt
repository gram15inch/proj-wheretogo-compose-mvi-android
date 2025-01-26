package com.dhkim139.wheretogo.mock.model

import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile

data class MockRemoteUser(
    val token: String = "",
    val profile: Profile = Profile(),
    val history: Map<HistoryType, HashSet<String>> = emptyMap()
)