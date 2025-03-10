package com.wheretogo.domain.repository


import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress

interface AddressRepository {
    suspend fun getAddress(address: String): Result<Address>

    suspend fun getAddressFromLatlng(latlng: LatLng): Result<String>

    suspend fun getAddressFromKeyword(query: String): Result<List<SimpleAddress>>
}