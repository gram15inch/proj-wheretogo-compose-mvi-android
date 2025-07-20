package com.wheretogo.presentation.state

import com.wheretogo.domain.model.user.Profile
import com.wheretogo.presentation.model.AdItem

data class SettingScreenState(
    val profile: Profile = Profile(),
    val isProfile: Boolean = false,
    val isLoading: Boolean = false,
    val isDialog: Boolean = false,
    val adItemGroup:List<AdItem> = emptyList()
)