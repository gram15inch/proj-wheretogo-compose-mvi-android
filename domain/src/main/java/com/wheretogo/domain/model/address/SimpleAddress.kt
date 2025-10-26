package com.wheretogo.domain.model.address

import com.wheretogo.domain.SearchType

data class SimpleAddress(val title: String, val address: String, val latlng: LatLng, val type: SearchType = SearchType.ADDRESS)