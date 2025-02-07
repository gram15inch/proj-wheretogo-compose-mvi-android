package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.AddressRemoteDatasourceImpl
import com.wheretogo.domain.model.map.LatLng
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class AddressTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var addressRemoteDatasourceImpl: AddressRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun geocodeTest(): Unit = runBlocking {
        val r = addressRemoteDatasourceImpl.geocode("경기도 성남시 분당구 불정로 6 NAVER그린팩토리")
        assertEquals(true, r.isNotEmpty())
        assertEquals("경기도 성남시 분당구 불정로 6 NAVER그린팩토리", r.first().road)
        assertEquals("경기도 성남시 분당구 정자동 178-1 NAVER그린팩토리", r.first().jibun)
        assertEquals(
            "6, Buljeong-ro, Bundang-gu, Seongnam-si, Gyeonggi-do, Republic of Korea",
            r.first().eng
        )
    }

    @Test
    fun reverseGeocodeTest(): Unit = runBlocking {
        val datasource = addressRemoteDatasourceImpl
        val r = datasource.reverseGeocode(LatLng(37.56661, 126.978388))
        assertEquals(true, r.isNotEmpty())
    }

    @Test
    fun getSimppleAddressFromKeywordTest(): Unit = runBlocking {
        val r = addressRemoteDatasourceImpl.getSimpleAddressFromKeyword("중미산")
        assertEquals(true, r.isNotEmpty())
        assertEquals("중미산", r.first().title)
    }

}