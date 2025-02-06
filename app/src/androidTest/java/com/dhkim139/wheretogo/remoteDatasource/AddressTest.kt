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
    fun getAddressWithLatLngTest(): Unit = runBlocking {
        val datasource = addressRemoteDatasourceImpl
        val r = datasource.getAddress(LatLng(37.56661, 126.978388))
        assertEquals(true, r.isNotEmpty())
    }

    @Test
    fun getAddressWithQueryTest(): Unit = runBlocking {
        val r = addressRemoteDatasourceImpl.getAddress("중미산")
        assertEquals(true, r.isNotEmpty())
        assertEquals("중미산", r.first().title)
    }
}