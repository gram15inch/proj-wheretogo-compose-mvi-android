package com.wheretogo.data.repositoryimpl


import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.repository.AddressRepository
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val remoteDatasource: AddressRemoteDatasource
) : AddressRepository {

    override suspend fun getAddress(latlng: LatLng): String {
        return remoteDatasource.getAddress(latlng)
    }

    override suspend fun getAddress(query: String): List<Address> {
        return remoteDatasource.getAddress(query)
    }
}