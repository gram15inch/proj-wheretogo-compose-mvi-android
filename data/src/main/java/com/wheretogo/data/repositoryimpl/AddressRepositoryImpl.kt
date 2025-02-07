package com.wheretogo.data.repositoryimpl


import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress
import com.wheretogo.domain.repository.AddressRepository
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val remoteDatasource: AddressRemoteDatasource
) : AddressRepository {
    private val cacheSimpleAddress = mutableMapOf<String, List<SimpleAddress>>()
    private val cacheAddress = mutableMapOf<String, List<Address>>()

    override suspend fun getAddress(address: String): Address {
        return cacheAddress.getOrPut(address + "address") {
            remoteDatasource.geocode(address)
        }.first()
    }

    override suspend fun getAddressFromLatlng(latlng: LatLng): String {
        return remoteDatasource.reverseGeocode(latlng)
    }

    override suspend fun getAddressFromKeyword(query: String): List<SimpleAddress> {
        return cacheSimpleAddress.getOrPut(query + "keyword") {
            remoteDatasource.getSimpleAddressFromKeyword(query)
        }
    }
}