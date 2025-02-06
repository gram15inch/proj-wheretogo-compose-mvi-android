package com.wheretogo.domain.repository


import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng

interface AddressRepository {
    suspend fun getAddress(latlng: LatLng): String

    suspend fun getAddress(query: String): List<Address>
}