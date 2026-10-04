package com.wheretogo.data.datasourceimpl

import android.os.Build
import android.text.Html
import com.wheretogo.data.DataBuildConfig
import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.data.datasourceimpl.service.NaverFreeApiService
import com.wheretogo.data.toDataError
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import javax.inject.Inject

class AddressRemoteDatasourceImpl @Inject constructor(
    private val naverFreeApiService: NaverFreeApiService,
    private val buildConfig: DataBuildConfig
) : AddressRemoteDatasource {

    override suspend fun getSimpleAddressFromKeyword(keyword: String): Result<List<SimpleAddress>> {
        val response = naverFreeApiService.getAddressFromKeyword(
            clientId = buildConfig.naverClientIdKey,
            clientSecret = buildConfig.naverClientSecretKey,
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