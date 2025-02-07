package com.wheretogo.data.datasource

import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress

interface AddressRemoteDatasource {

    suspend fun geocode(address: String): List<Address>

    suspend fun reverseGeocode(latlng: LatLng): String

    suspend fun getSimpleAddressFromKeyword(keyword: String): List<SimpleAddress>

}