package com.wheretogo.data.model.map

import kotlinx.serialization.Serializable

@Serializable
data class DataLatLng(val latitude: Double=0.0, val longitude: Double=0.0)