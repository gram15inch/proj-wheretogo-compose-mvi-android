package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.SimpleAddress

data class SearchBarState(
    val isVisible :Boolean = true,
    val isLoading: Boolean = false,
    val simpleAddressGroup: List<SimpleAddress> = emptyList()
)