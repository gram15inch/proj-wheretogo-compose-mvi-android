package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.domain.model.address.Address
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import javax.inject.Inject

class MockAddressRemoteDatasourceImpl @Inject constructor() : AddressRemoteDatasource {
    override suspend fun reverseGeocode(latlng: LatLng): Result<String> {
        return Result.success("")
    }

    override suspend fun getSimpleAddressFromKeyword(keyword: String): Result<List<SimpleAddress>> {
        return Result.success(emptyList())
    }

    override suspend fun geocode(address: String): Result<List<Address>> {
        return Result.success(emptyList())
    }
}