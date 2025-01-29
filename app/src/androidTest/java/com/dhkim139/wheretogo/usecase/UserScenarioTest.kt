package com.dhkim139.wheretogo.usecase

import android.util.Log
import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.domain.UseCaseFailType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.user.AuthData
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import jakarta.inject.Inject
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class UserScenarioTest {
    val tag = "tst_user"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var signInUseCase: UserSignInUseCase

    @Inject
    lateinit var signOutUseCase: UserSignOutUseCase

    @Inject
    lateinit var signUpAndSignInUseCase: UserSignUpAndSignInUseCase

    @Inject
    lateinit var getUserProfileStreamUseCase: GetUserProfileStreamUseCase

    @Inject
    lateinit var getHistoryStreamUseCase: GetHistoryStreamUseCase

    @Inject
    lateinit var user: MockRemoteUser

    @Test // 기존 사용자 로그인 - 로그아웃
    fun scenario1(): Unit = runBlocking {
        val user1 = user
        getUserProfileStreamUseCase().first().assertEmpty()

        signInUseCase().success()
        val profile = getUserProfileStreamUseCase().first()
        assertEquals(user1.profile.uid, profile.data!!.uid)
        getHistoryStreamUseCase().first().assertEquals(user1.history)
        signOutUseCase().success()

        getUserProfileStreamUseCase().first().assertEmpty()
        getHistoryStreamUseCase().first().assertEmpty()
    }

    @Test// 새로운 사용자 회원가입 - 로그인 후 알수없는 사용자 로그인 실패
    fun scenario2():Unit = runBlocking {
        val resistUser = AuthData(
            uid ="uid1",
            email = "email1",
            userName = "userName1"
        )
        val unknownUser = AuthData(
            uid ="uid2",
            email = "email2",
            userName = "userName2"
        )
        getUserProfileStreamUseCase().first().assertEmpty()

        signUpAndSignInUseCase(resistUser).success()
        getUserProfileStreamUseCase().first().data!!.assertEquals(resistUser)

        signOutUseCase().success()
        getUserProfileStreamUseCase().first().assertEmpty()
        getHistoryStreamUseCase().first().assertEmpty()

        signInUseCase(unknownUser).fail()
        signInUseCase(resistUser).success()
        getUserProfileStreamUseCase().first().data!!.assertEquals(resistUser)
        signOutUseCase().success()
    }

    private fun UseCaseResponse<String>.success() {

        this.apply {
            Log.d(tag, this.toString())
            assertEquals(UseCaseResponse.Status.Success, this.status)
        }
    }

    private fun UseCaseResponse<String>.fail() {
        this.apply {
            Log.d(tag, this.toString())
            assertEquals(UseCaseResponse.Status.Fail, this.status)
            assertEquals(UseCaseFailType.INVALID_USER, failType)
        }
    }

    private fun UseCaseResponse<Profile>.assertEmpty() {
        this@assertEmpty.apply {
            Log.d(tag, this.toString())
            assertEquals(UseCaseResponse.Status.Fail, status)
            assertEquals(UseCaseFailType.INVALID_USER, failType)
        }
    }

    private fun History.assertEquals(history: History) {
        Log.d(tag, "${history}")
        assertEquals(this, history)
    }

    private fun History.assertEmpty() {
        assertEquals(commentGroup, hashSetOf<String>())
        assertEquals(courseGroup, hashSetOf<String>())
        assertEquals(likeGroup, hashSetOf<String>())
        assertEquals(bookmarkGroup, hashSetOf<String>())
        assertEquals(checkpointGroup, hashSetOf<String>())
        assertEquals(reportGroup, hashSetOf<String>())
    }

    private fun Profile.assertEquals(authData: AuthData){
        assertEquals(authData.uid, uid)
        assertEquals(authData.email, private.mail)
        assertEquals(authData.userName, public.name)
    }
}