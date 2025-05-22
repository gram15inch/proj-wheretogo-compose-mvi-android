package com.wheretogo.presentation.state

import com.wheretogo.presentation.model.SearchBarItem

data class SearchBarState(
    val isVisible:Boolean = true,
    val isLoading: Boolean = false,
    val isEmptyVisible: Boolean = false,
    val searchBarItemGroup: List<SearchBarItem> = emptyList()
)