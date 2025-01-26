package com.dhkim139.wheretogo.usecase

import android.util.Log
import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import jakarta.inject.Inject
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
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

    @Test // 유저 로그인, 로그아웃
    fun scenario1(): Unit = runBlocking {
        getUserProfileStreamUseCase().assertEmpty()

        signInUseCase().success()
        val profile = getUserProfileStreamUseCase().first()
        assertEquals(user.profile.uid, profile.uid)
        getHistoryStreamUseCase().first().assertEquals(user.history)
        signOutUseCase().success()

        getUserProfileStreamUseCase().assertEmpty()
        getHistoryStreamUseCase().first().assertEmpty()
    }


    private fun UseCaseResponse.success() {
        this.apply {
            Log.d(tag, "${this}")
            assertEquals(UseCaseResponse.Status.Success, this.status)
        }
    }

    private fun UseCaseResponse.fail() {
        this.apply {
            Log.d(tag, "${this}")
            assertEquals(UseCaseResponse.Status.Fail, this.status)
        }
    }

    private fun Flow<Any>.assertEmpty() {
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                this@assertEmpty.first().apply {
                    Log.d(tag,this.toString())
                }
            }
        }
    }

    private fun History.assertEquals(history: Map<HistoryType, HashSet<String>>) {
        assertEquals(commentGroup, history.get(HistoryType.COMMENT))
        assertEquals(courseGroup, history.get(HistoryType.COURSE))
        assertEquals(likeGroup, history.get(HistoryType.LIKE))
        assertEquals(bookmarkGroup, history.get(HistoryType.BOOKMARK))
        assertEquals(checkpointGroup, history.get(HistoryType.CHECKPOINT))
        assertEquals(reportGroup, history.get(HistoryType.REPORT))
    }

    private fun History.assertEmpty() {
        assertEquals(commentGroup, hashSetOf<String>())
        assertEquals(courseGroup, hashSetOf<String>())
        assertEquals(likeGroup, hashSetOf<String>())
        assertEquals(bookmarkGroup, hashSetOf<String>())
        assertEquals(checkpointGroup, hashSetOf<String>())
        assertEquals(reportGroup, hashSetOf<String>())
    }
}