package com.wheretogo.data.model.route

import com.wheretogo.data.DATA_NULL
import com.wheretogo.data.model.map.DataLatLng

data class RemoteRoute(val routeId: String = DATA_NULL, val points: List<DataLatLng> = emptyList())