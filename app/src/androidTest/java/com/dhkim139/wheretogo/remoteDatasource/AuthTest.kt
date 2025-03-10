package com.dhkim139.wheretogo.remoteDatasource

import android.util.Log
import com.dhkim139.wheretogo.feature.getContext
import com.dhkim139.wheretogo.feature.pickAccount
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.wheretogo.data.datasourceimpl.AuthRemoteDatasourceImpl
import com.wheretogo.presentation.feature.googleAuthOnDevice
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class AuthTest {
    val tag = "tst_auth"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var authRemoteDatasourceImpl: AuthRemoteDatasourceImpl

    @Inject
    lateinit var getGoogleIdOption: GetGoogleIdOption

    @Before
    fun init() {
        hiltRule.inject()
    }


    @Test
    fun deviceAuthTest(): Unit = runBlocking {
        val context = getContext()
        launch { pickAccount("navi") }
        val authRequest = googleAuthOnDevice(getGoogleIdOption, context)

        assertTrue(authRequest?.authToken != null)
        Log.d(tag, "${authRequest!!.authToken}")
    }


    @Test
    fun firebaseAuthTest(): Unit = runBlocking {
        val context = getContext()
        val firebase = FirebaseAuth.getInstance()
        launch { pickAccount("navi") }
        val authRequest = googleAuthOnDevice(getGoogleIdOption, context)

        assertTrue(firebase.currentUser == null)
        val authResponse =
            authRemoteDatasourceImpl.authGoogleWithFirebase(authRequest!!.authToken!!)
        assertTrue(authResponse != null)
        Log.d(tag, "${authResponse}")

        assertTrue(firebase.currentUser?.email != null)
        Log.d(tag, "${firebase.currentUser?.email}")
        authRemoteDatasourceImpl.signOutOnFirebase()

        assertTrue(firebase.currentUser == null)
    }

}