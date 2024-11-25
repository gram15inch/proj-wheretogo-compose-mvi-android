package com.wheretogo.data.datasource

import com.wheretogo.domain.model.user.Profile

interface UserRemoteDatasource {
    suspend fun setProfile(profile: Profile): Boolean
    suspend fun getProfile(uid: String): Profile?
}