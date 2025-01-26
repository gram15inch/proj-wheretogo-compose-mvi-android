package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.AuthRemoteDatasourceImpl
import com.wheretogo.domain.toProfile
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotEquals
import javax.inject.Inject


@HiltAndroidTest
class AuthTest {
    val tag = "tst_auth"
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var  authRemoteDatasourceImpl : AuthRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun authOnDeviceTest():Unit = runBlocking{
        val profile = authRemoteDatasourceImpl.authOnDevice().data?.toProfile()
        assertNotEquals(null, profile)
    }
}