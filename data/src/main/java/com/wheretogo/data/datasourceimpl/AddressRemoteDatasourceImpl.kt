package com.wheretogo.data.datasourceimpl

import android.os.Build
import android.text.Html
import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverFreeApiService
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.data.toDataError
import com.wheretogo.domain.model.address.Address
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import com.wheretogo.domain.model.app.AppBuildConfig
import javax.inject.Inject

class AddressRemoteDatasourceImpl @Inject constructor(
    private val naverApiService: NaverMapApiService,
    private val naverFreeApiService: NaverFreeApiService,
    private val appBuildConfig: AppBuildConfig
) : AddressRemoteDatasource {

    private fun convertLatLng(latlng: LatLng): String = "${latlng.longitude}, ${latlng.latitude}"

    override suspend fun geocode(address: String): Result<List<Address>> {
        val response = naverApiService.geocode(
            clientId = appBuildConfig.naverMapsApigwClientIdKey,
            clientSecret = appBuildConfig.naverMapsApigwClientSecretkey,
            accept = "application/json",
            query = address,
            count = "1"
        )
        if (!response.isSuccessful)
            return Result.failure(response.toDataError())

        val addGroup = response.body()?.addresses?.map {
            Address(
                jibun = it.jibunAddress,
                road = it.roadAddress,
                eng = it.englishAddress,
                latLng = LatLng(it.y.toDouble(), it.x.toDouble())
            )
        } ?: emptyList()
        return Result.success(addGroup)
    }

    override suspend fun reverseGeocode(latlng: LatLng): Result<String> {
        val response = naverApiService.reverseGeocode(
            clientId = appBuildConfig.naverMapsApigwClientIdKey,
            clientSecret = appBuildConfig.naverMapsApigwClientSecretkey,
            coords = convertLatLng(latlng),
            output = "json"
        )

        if (!response.isSuccessful)
            return Result.failure(response.toDataError())


        val region = response.body()?.results?.firstOrNull()?.region
        val addr = region?.run { "${area1.name} ${area2.name} ${area3.name} ${area4.name}" } ?: ""
        return Result.success(addr)
    }

    override suspend fun getSimpleAddressFromKeyword(keyword: String): Result<List<SimpleAddress>> {
        val response = naverFreeApiService.getAddressFromKeyword(
            clientId = appBuildConfig.naverClientIdKey,
            clientSecret = appBuildConfig.naverClientSecretKey,
            query = keyword,
            display = 10,
            start = 1,
            sort = "random"
        )

        if (!response.isSuccessful)
            return Result.failure(response.toDataError())


        val addrGroup = response.body()?.items?.map {
            val latlng = convertMapXY(it.mapx, it.mapy)
            SimpleAddress(removeHtmlTags(it.title), it.address, latlng)
        } ?: emptyList()
        return Result.success(addrGroup)
    }

    private fun removeHtmlTags(htmlText: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(htmlText).toString()
        }
    }

    private fun convertMapXY(x: String, y: String): LatLng {
        return LatLng(y.toDouble() / 10_000_000, x.toDouble() / 10_000_000)
    }
}