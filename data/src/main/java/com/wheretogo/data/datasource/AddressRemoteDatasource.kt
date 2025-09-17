package com.wheretogo.data.datasource

import com.wheretogo.domain.model.address.Address
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress

interface AddressRemoteDatasource {

    suspend fun geocode(address: String): Result<List<Address>>

    suspend fun reverseGeocode(latlng: LatLng): Result<String>

    suspend fun getSimpleAddressFromKeyword(keyword: String): Result<List<SimpleAddress>>

}