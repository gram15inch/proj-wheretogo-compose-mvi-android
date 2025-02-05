package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import javax.inject.Inject

class MockAddressRemoteDatasourceImpl @Inject constructor() : AddressRemoteDatasource {
    override suspend fun getAddress(latlng: LatLng): String {
        return ""
    }

    override suspend fun getAddress(query: String): List<Address> {
        return emptyList()
    }
}