package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.AuthRemoteDatasourceImpl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject


@HiltAndroidTest
class AuthTest {
    val tag = "tst_auth"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var authRemoteDatasourceImpl: AuthRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }
}