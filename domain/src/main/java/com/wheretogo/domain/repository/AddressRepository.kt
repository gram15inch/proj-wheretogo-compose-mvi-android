package com.wheretogo.domain.repository


import com.wheretogo.domain.model.address.SimpleAddress

interface AddressRepository {

    suspend fun getAddressFromKeyword(query: String): Result<List<SimpleAddress>>
}