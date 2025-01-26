package com.dhkim139.wheretogo.remoteDatasource

import androidx.test.platform.app.InstrumentationRegistry
import com.wheretogo.data.datasourceimpl.UserRemoteDatasourceImpl
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class UserTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userRemoteDatasourceImpl: UserRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dhkim139.wheretogo", appContext.packageName)
    }

    @Test
    fun getAndSetProfileTest(): Unit = runBlocking {
        val remoteDatasource = userRemoteDatasourceImpl

        val p1 = Profile(
            uid = "uid1",
            public = ProfilePublic(
                name = "name"
            ),
            private = ProfilePrivate(
                mail = "mail",
                authCompany = "google",
                lastVisited = 0L,
                accountCreation = 0L,
                isAdRemove = false
            ),
        )

        assertEquals(true, remoteDatasource.setProfilePublic(p1.uid, p1.public))
        val public = remoteDatasource.getProfilePublic(p1.uid)
        assertEquals(p1.public, public)

        assertEquals(true, remoteDatasource.setProfilePrivate(p1.uid, p1.private))
        val private = remoteDatasource.getProfilePrivate(p1.uid)
        assertEquals(p1.private, private)

        assertEquals(true, remoteDatasource.deleteProfile(p1.uid))
    }

    @Test
    fun setHistoryTest(): Unit = runBlocking {
        val remoteDatasource = userRemoteDatasourceImpl
        val uid = "xXGqqUYVViM42AoWPPDoYc0gAG12"
        val hid = "rp_comment1"
        remoteDatasource.addHistory(
            uid = uid,
            historyId = hid,
            type = HistoryType.REPORT
        )

        val hid2 = remoteDatasource.getHistoryGroup(uid, HistoryType.REPORT)

        assertEquals(true, hid2.second.isNotEmpty())
        assertEquals(hid, hid2.second.first())
    }

}