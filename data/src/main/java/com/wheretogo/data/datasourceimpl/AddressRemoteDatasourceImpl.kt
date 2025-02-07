package com.wheretogo.data.datasourceimpl

import android.os.Build
import android.text.Html
import android.util.Log
import com.wheretogo.data.BuildConfig
import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverFreeApiService
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.domain.model.map.Address
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress
import javax.inject.Inject

class AddressRemoteDatasourceImpl @Inject constructor(
    private val naverApiService: NaverMapApiService,
    private val naverFreeApiService: NaverFreeApiService,
) : AddressRemoteDatasource {

    private fun convertLatLng(latlng: LatLng): String = "${latlng.longitude}, ${latlng.latitude}"

    override suspend fun geocode(address: String): List<Address> {
        val msg = naverApiService.geocode(
            clientId = BuildConfig.NAVER_APIGW_CLIENT_ID_KEY,
            clientSecret = BuildConfig.NAVER_APIGW_CLIENT_SECRET_KEY,
            accept = "application/json",
            query = address,
            count = "1"
        )
        if (msg.code() == 200) {
            val addGroup = msg.body()?.addresses
            return addGroup?.map {
                Address(
                    jibun = it.jibunAddress,
                    road = it.roadAddress,
                    eng = it.englishAddress,
                    latLng = LatLng(it.x.toDouble(), it.y.toDouble())
                )
            } ?: emptyList()

        } else {
            return emptyList()
        }
    }

    override suspend fun reverseGeocode(latlng: LatLng): String {
        val msg = naverApiService.reverseGeocode(
            clientId = BuildConfig.NAVER_APIGW_CLIENT_ID_KEY,
            clientSecret = BuildConfig.NAVER_APIGW_CLIENT_SECRET_KEY,
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

    override suspend fun getSimpleAddressFromKeyword(keyword: String): List<SimpleAddress> {
        val response = naverFreeApiService.getAddressFromKeyword(
            clientId = BuildConfig.NAVER_CLIENT_ID_KEY,
            clientSecret = BuildConfig.NAVER_CLIENT_SECRET_KEY,
            query = keyword,
            display = 10,
            start = 1,
            sort = "random"
        )

        return if (response.code() == 200) {
            response.body()?.items?.map {
                SimpleAddress(removeHtmlTags(it.title), it.address)
            } ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun removeHtmlTags(htmlText: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(htmlText).toString()
        }
    }
}