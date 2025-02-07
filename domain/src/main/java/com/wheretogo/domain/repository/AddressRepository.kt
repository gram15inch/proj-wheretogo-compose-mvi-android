package com.wheretogo.domain.repository


import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.SimpleAddress
import com.wheretogo.domain.model.map.LatLng

interface AddressRepository {
    suspend fun getAddress(address: String): Address

    suspend fun getAddressFromLatlng(latlng: LatLng): String

    suspend fun getAddressFromKeyword(query: String): List<SimpleAddress>
}