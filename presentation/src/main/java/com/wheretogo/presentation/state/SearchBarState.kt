package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.Address

data class SearchBarState(
    val isLoading: Boolean = false,
    val addressGroup: List<Address> = emptyList()
)