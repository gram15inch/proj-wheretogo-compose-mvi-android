package com.wheretogo.presentation.model

import com.wheretogo.domain.model.address.LatLng

data class SearchBarItem(
    val label: String,
    val address: String,
    val latlng: LatLng? = null,
    val isCourse: Boolean = false,
    val isHighlight: Boolean = false
)