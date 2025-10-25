package com.wheretogo.domain.repository

import com.wheretogo.domain.model.auth.SyncToken
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile

interface GuestRepository {
    suspend fun syncUser(syncToken: SyncToken): Result<Pair<Profile, History>>
}