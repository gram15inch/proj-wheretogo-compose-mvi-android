package com.wheretogo.presentation.model

import com.wheretogo.domain.model.map.LatLng

data class SearchBarItem(val label: String, val address: String, val latlng: LatLng? = null)