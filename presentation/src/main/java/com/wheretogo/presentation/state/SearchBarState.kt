package com.wheretogo.presentation.state

import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.model.SearchBarItem

data class SearchBarState(
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val isAdVisible: Boolean = false,
    val isEmptyVisible: Boolean = false,
    val searchBarItemGroup: List<SearchBarItem> = emptyList(),
    val adItemGroup: List<AdItem> = emptyList(),
)