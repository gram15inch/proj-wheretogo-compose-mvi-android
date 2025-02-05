package com.wheretogo.data.datasource


import com.wheretogo.domain.model.map.LatLng

interface AddressRemoteDatasource {

    suspend fun getAddress(latlng: LatLng): String

}