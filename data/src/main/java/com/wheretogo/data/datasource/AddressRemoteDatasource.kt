package com.wheretogo.data.datasource

import com.wheretogo.domain.model.address.SimpleAddress

interface AddressRemoteDatasource {

    suspend fun getSimpleAddressFromKeyword(keyword: String): Result<List<SimpleAddress>>

}