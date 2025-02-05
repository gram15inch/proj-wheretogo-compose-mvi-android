package com.wheretogo.data.datasourceimpl

import android.util.Log
import com.wheretogo.data.BuildConfig
import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverFreeApiService
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import javax.inject.Inject

class AddressRemoteDatasourceImpl @Inject constructor(
    private val naverApiService: NaverMapApiService,
    private val naverFreeApiService: NaverFreeApiService,
) : AddressRemoteDatasource {

    private fun convertLatLng(latlng: LatLng): String = "${latlng.longitude}, ${latlng.latitude}"

    override suspend fun getAddress(latlng: LatLng): String {
        val msg = naverApiService.getAddress(
            clientId = BuildConfig.NAVER_CLIENT_ID_KEY,
            clientSecret = BuildConfig.NAVER_CLIENT_SECRET_KEY,
            coords = convertLatLng(latlng),
            output = "json"
        )

        if (msg.code() == 200) {
            val region = msg.body()?.results?.firstOrNull()?.region
            val addr =
                region?.run { "${area1.name} ${area2.name} ${area3.name} ${area4.name}" } ?: ""
            return addr
        } else {
            return ""
        }
    }

}