package com.wheretogo.domain.repository


import com.wheretogo.domain.model.address.Address
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress

interface AddressRepository {
    suspend fun getAddress(address: String): Result<Address>

    suspend fun getAddressFromLatlng(latlng: LatLng): Result<String>

    suspend fun getAddressFromKeyword(query: String): Result<List<SimpleAddress>>
}