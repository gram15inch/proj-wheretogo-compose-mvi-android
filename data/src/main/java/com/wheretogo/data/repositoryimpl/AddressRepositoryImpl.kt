package com.wheretogo.data.repositoryimpl


import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.data.feature.catchNotFound
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.domain.model.address.Address
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import com.wheretogo.domain.repository.AddressRepository
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val remoteDatasource: AddressRemoteDatasource
) : AddressRepository {
    private val cacheSimpleAddress = mutableMapOf<String, List<SimpleAddress>>()
    private val cacheAddress = mutableMapOf<String, List<Address>>()

    override suspend fun getAddress(address: String): Result<Address> {
        return runCatching {
            val cache = cacheAddress[address + "address"]
            if(cache!=null)
              return Result.success(cache.first())
        }.mapSuccess {
            remoteDatasource.geocode(address)
        }.mapCatching {
            cacheAddress.put(address + "address", it)
            it.first()
        }.mapDataError().mapDomainError()
    }

    override suspend fun getAddressFromLatlng(latlng: LatLng): Result<String> {
        return remoteDatasource.reverseGeocode(latlng).mapDataError().mapDomainError()

    }

    override suspend fun getAddressFromKeyword(query: String): Result<List<SimpleAddress>> {
        return runCatching {
            val cache= cacheSimpleAddress[query + "keyword"]
            if(cache!=null)
                return Result.success(cache)
        }.mapSuccess {
            remoteDatasource.getSimpleAddressFromKeyword(query)
        }.catchNotFound{
            Result.success(emptyList())
        }.onSuccess {
            cacheSimpleAddress.put(query + "keyword", it)
        }.mapDataError().mapDomainError()
    }
}