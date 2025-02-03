package com.dhkim139.wheretogo.remoteDatasource

import androidx.test.platform.app.InstrumentationRegistry
import com.wheretogo.data.datasourceimpl.UserRemoteDatasourceImpl
import com.wheretogo.data.model.user.ProfilePublic
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePublic
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
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

    private fun getLocalProfile(id: String): Profile {
        val public1 = ProfilePublic(
            name = "name$id",
            hashMail = hashSha256("mail$id"),
        )
        val private1 = ProfilePrivate(
            mail = "mail$id",
            authCompany = "google",
            lastVisited = 0L,
            accountCreation = 0L,
            isAdRemove = false
        )
        return public1.toProfile().copy(private = private1)
    }

    @Test
    fun getAndSetProfileTest(): Unit = runBlocking {
        val remoteDatasource = userRemoteDatasourceImpl
        val p1 = getLocalProfile("1")
        assertEquals(true, remoteDatasource.setProfilePublic(p1.toProfilePublic()))
        assertEquals(p1.toProfilePublic(), remoteDatasource.getProfilePublic(p1.uid))

        assertEquals(true, remoteDatasource.setProfilePrivate(p1.uid, p1.private))
        assertEquals(p1.private, remoteDatasource.getProfilePrivate(p1.uid))

        assertEquals(true, remoteDatasource.deleteProfile(p1.uid))
    }

    @Test
    fun getProfileByEmail(): Unit = runBlocking {
        val mail1 = "mail1"
        val mail2 = "mail2"
        val mail3 = "mail3"
        val p1 = getLocalProfile("1").copy(hashMail = hashSha256(mail1))
        val p2 = getLocalProfile("2").copy(hashMail = hashSha256(mail2))

        userRemoteDatasourceImpl.setProfilePublic(p1.toProfilePublic())
        userRemoteDatasourceImpl.setProfilePublic(p2.toProfilePublic())

        assertEquals(
            p1.toProfilePublic(),
            userRemoteDatasourceImpl.getProfilePublicWithMail(hashSha256(mail1))
        )
        assertEquals(
            p2.toProfilePublic(),
            userRemoteDatasourceImpl.getProfilePublicWithMail(hashSha256(mail2))
        )
        assertEquals(null, userRemoteDatasourceImpl.getProfilePublicWithMail(hashSha256(mail3)))

        assertEquals(true, userRemoteDatasourceImpl.deleteProfile(p1.uid))
        assertEquals(true, userRemoteDatasourceImpl.deleteProfile(p2.uid))
    }

    @Test
    fun setHistoryTest(): Unit = runBlocking {
        val remoteDatasource = userRemoteDatasourceImpl
        val uid = "xXGqqUYVViM42AoWPPDoYc0gAG12"
        val hid = "rp_comment1"
        remoteDatasource.addHistory(
            uid = uid,
            historyId = hid,
            type = HistoryType.REPORT_CONTENT
        )

        val hid2 = remoteDatasource.getHistoryGroup(uid, HistoryType.REPORT_CONTENT)

        assertEquals(true, hid2.second.isNotEmpty())
        assertEquals(hid, hid2.second.first())
    }
}