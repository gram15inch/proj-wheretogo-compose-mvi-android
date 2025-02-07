package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress
import javax.inject.Inject

class MockAddressRemoteDatasourceImpl @Inject constructor() : AddressRemoteDatasource {
    override suspend fun reverseGeocode(latlng: LatLng): String {
        return ""
    }

    override suspend fun getSimpleAddressFromKeyword(keyword: String): List<SimpleAddress> {
        return emptyList()
    }

    override suspend fun geocode(address: String): List<Address> {
        return emptyList()
    }
}