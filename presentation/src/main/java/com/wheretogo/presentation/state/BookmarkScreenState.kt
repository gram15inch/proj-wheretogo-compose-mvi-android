package com.wheretogo.presentation.state

import com.wheretogo.domain.model.map.Journey

data class BookmarkScreenState(val data: List<Journey> = emptyList())
