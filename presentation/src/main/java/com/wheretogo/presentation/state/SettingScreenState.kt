package com.wheretogo.presentation.state

import com.wheretogo.domain.model.user.Profile

data class SettingScreenState(
    val profile: Profile = Profile(),
    val isProfile: Boolean = false,
    val isLoading: Boolean = false
)