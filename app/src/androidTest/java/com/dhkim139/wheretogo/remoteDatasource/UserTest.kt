package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.datasourceimpl.UserRemoteDatasourceImpl
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.data.toProfile
import com.wheretogo.data.toProfilePublic
import com.wheretogo.data.toRemoteProfilePrivate
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

    private fun getLocalProfile(id: String): Profile {
        val public1 = RemoteProfilePublic(
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
        return public1.toProfile().copy(uid = "user_test$id",private = private1)
    }

    @Test
    fun get_set_delete_user_should_work_correctly(): Unit = runBlocking {
        val remoteDatasource = userRemoteDatasourceImpl
        val p1 = getLocalProfile("1")
        remoteDatasource.setProfilePublic(p1.toProfilePublic())
        assertEquals(p1.toProfilePublic(), remoteDatasource.getProfilePublic(p1.uid))

        remoteDatasource.setProfilePrivate(p1.uid, p1.private.toRemoteProfilePrivate())
        assertEquals(p1.private, remoteDatasource.getProfilePrivate(p1.uid))

        remoteDatasource.deleteProfile(p1.uid)
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

        userRemoteDatasourceImpl.deleteProfile(p1.uid)
        userRemoteDatasourceImpl.deleteProfile(p2.uid)
    }

    @Test
    fun get_set_history_should_work_correctly(): Unit = runBlocking {
        val remoteDatasource = userRemoteDatasourceImpl
        val p1 = getLocalProfile("1")
        remoteDatasource.setProfilePublic(p1.toProfilePublic())
        val inputHistory1 ="${p1.uid}_his1"
        val inputHistory2 ="${p1.uid}_his2"
        remoteDatasource.addHistory(
            uid =  p1.uid,
            historyId = inputHistory1,
            type = HistoryType.REPORT_CONTENT
        )
        remoteDatasource.addHistory(
            uid =  p1.uid,
            historyId = inputHistory2,
            type = HistoryType.REPORT_CONTENT
        )

        val outputHistory = remoteDatasource.getHistoryGroup(p1.uid, HistoryType.REPORT_CONTENT)
        assertEquals(true, inputHistory1 in outputHistory.second)
        assertEquals(true, inputHistory2 in outputHistory.second)

        val wrapper = RemoteHistoryGroupWrapper(outputHistory.second.filter { inputHistory1 ==it },HistoryType.REPORT_CONTENT)
        remoteDatasource.setHistoryGroup(p1.uid, wrapper)
        val outputHistory2 = remoteDatasource.getHistoryGroup(p1.uid, HistoryType.REPORT_CONTENT)
        assertEquals(true, inputHistory1 in outputHistory2.second)
        assertEquals(false, inputHistory2 in outputHistory2.second)

        remoteDatasource.deleteHistory(p1.uid,HistoryType.REPORT_CONTENT)
        val outputHistory3 = remoteDatasource.getHistoryGroup(p1.uid, HistoryType.REPORT_CONTENT)
        assertEquals(false, inputHistory1 in outputHistory3.second)
        assertEquals(false, inputHistory2 in outputHistory3.second)

        remoteDatasource.deleteProfile(p1.uid)
    }
}