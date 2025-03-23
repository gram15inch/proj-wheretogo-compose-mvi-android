package com.dhkim139.wheretogo.remoteDatasource

import com.dhkim139.wheretogo.feature.getContext
import com.dhkim139.wheretogo.feature.pickAccount
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wheretogo.data.datasourceimpl.AuthRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CommentRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.ReportRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.UserRemoteDatasourceImpl
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.presentation.feature.googleAuthOnDevice
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
class AccessTest {
    val tag = "tst_access"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // 테스트 실행전 파이어베이스 규칙 변경 필요

    @Before
    fun init() {
        hiltRule.inject()
    }

    @After
    fun clear(): Unit = runBlocking {
        logout()
    }

    @Inject
    lateinit var getGoogleIdOption: GetGoogleIdOption

    @Inject
    lateinit var userRemoteDatasourceImpl: UserRemoteDatasourceImpl

    @Inject
    lateinit var authRemoteDatasourceImpl: AuthRemoteDatasourceImpl

    @Inject
    lateinit var courseRemoteDatasourceImpl: CourseRemoteDatasourceImpl

    @Inject
    lateinit var checkPointRemoteDatasourceImpl: CheckPointRemoteDatasourceImpl

    @Inject
    lateinit var commentRemoteDatasourceImpl: CommentRemoteDatasourceImpl

    @Inject
    lateinit var reportRemoteDatasourceImpl: ReportRemoteDatasourceImpl


    @Test
    fun userAccessTest(): Unit = runBlocking {
        val naviProfile = login("navi")

        val publicProfile = userRemoteDatasourceImpl.getProfilePublic(naviProfile.uid)
        val privateProfile = userRemoteDatasourceImpl.getProfilePrivate(naviProfile.uid)
        assertTrue(publicProfile != null)
        assertTrue(privateProfile != null)

        logout()

        val wherertogoProfile = login("wheretogo")
        userRemoteDatasourceImpl.getProfilePublic(wherertogoProfile.uid).let {
            assertTrue(it != null)
            assertEquals(wherertogoProfile.userName, it!!.name)
        }
        userRemoteDatasourceImpl.getProfilePrivate(wherertogoProfile.uid).let {
            assertTrue(it != null)
            assertEquals(wherertogoProfile.email, it!!.mail)
        }


        userRemoteDatasourceImpl.getProfilePublic(naviProfile.uid).let {
            assertTrue(it != null)
            assertEquals(naviProfile.userName, it!!.name)
        }
        assertThrowsPermission {
            userRemoteDatasourceImpl.getProfilePrivate(naviProfile.uid)
        }

        logout()
    }


    @Test
    fun courseAccessTest(): Unit = runBlocking {
        val naviProfile = login("navi")
        val naviCourse = RemoteCourse("naviCourse", userId = naviProfile.uid)
        courseRemoteDatasourceImpl.setCourse(naviCourse)
        logout()

        val wheretogoProfile = login("wheretogo")
        val wheretogoCourse = RemoteCourse("wheretogoCourse", userId = wheretogoProfile.uid)
        courseRemoteDatasourceImpl.setCourse(wheretogoCourse)
        courseRemoteDatasourceImpl.removeCourse(wheretogoCourse.courseId)
        assertThrowsPermission { courseRemoteDatasourceImpl.setCourse(naviCourse) }
        assertThrowsPermission { courseRemoteDatasourceImpl.removeCourse(naviCourse.courseId) }
        logout()

        login("navi")
        courseRemoteDatasourceImpl.removeCourse(naviCourse.courseId)
        logout()

        courseRemoteDatasourceImpl.getCourseGroupByGeoHash("A", "A")
        assertThrowsPermission { courseRemoteDatasourceImpl.setCourse(wheretogoCourse) }
        assertThrowsPermission { courseRemoteDatasourceImpl.removeCourse(wheretogoCourse.courseId) }
    }

    @Test
    fun checkpointAccessTest(): Unit = runBlocking {
        val naviProfile = login("navi")
        val naviCheckpoint = RemoteCheckPoint("naviCheckpoint", userId = naviProfile.uid)
        checkPointRemoteDatasourceImpl.setCheckPoint(naviCheckpoint)
        logout()

        val wheretogoProfile = login("wheretogo")
        val wheretogoCheckpoint =
            RemoteCheckPoint("wheretogoCheckpoint", userId = wheretogoProfile.uid)
        checkPointRemoteDatasourceImpl.setCheckPoint(wheretogoCheckpoint)
        checkPointRemoteDatasourceImpl.removeCheckPoint(wheretogoCheckpoint.checkPointId)
        assertThrowsPermission { checkPointRemoteDatasourceImpl.setCheckPoint(naviCheckpoint) }
        assertThrowsPermission { checkPointRemoteDatasourceImpl.removeCheckPoint(naviCheckpoint.checkPointId) }
        logout()

        login("navi")
        checkPointRemoteDatasourceImpl.removeCheckPoint(naviCheckpoint.checkPointId)
        logout()

        assertThrowsPermission { checkPointRemoteDatasourceImpl.setCheckPoint(wheretogoCheckpoint) }
        assertThrowsPermission { checkPointRemoteDatasourceImpl.removeCheckPoint(wheretogoCheckpoint.checkPointId) }

    }

    @Test
    fun commentAccessTest(): Unit = runBlocking {

        val naviProfile = login("navi")
        val naviCheckpoint = RemoteCheckPoint("naviCheckpoint", userId = naviProfile.uid)
        val naviComment = RemoteComment(
            "naviComment",
            commentGroupId = naviCheckpoint.checkPointId,
            userId = naviProfile.uid
        )
        checkPointRemoteDatasourceImpl.setCheckPoint(naviCheckpoint)
        commentRemoteDatasourceImpl.setCommentInCheckPoint(naviComment, true)
        commentRemoteDatasourceImpl.getCommentGroupInCheckPoint(naviCheckpoint.checkPointId)
        logout()

        commentRemoteDatasourceImpl.getCommentGroupInCheckPoint(naviCheckpoint.checkPointId)
        assertThrowsPermission {
            commentRemoteDatasourceImpl.setCommentInCheckPoint(
                naviComment,
                true
            )
        }
        assertThrowsPermission { commentRemoteDatasourceImpl.updateCommentInCheckPoint(naviComment) }

        login("navi")
        commentRemoteDatasourceImpl.removeCommentGroupInCheckPoint(naviComment.commentGroupId)
        checkPointRemoteDatasourceImpl.removeCheckPoint(naviCheckpoint.checkPointId)
        logout()
    }

    @Test
    fun reportAccessTest(): Unit = runBlocking {
        val naviProfile = login("navi")
        val naviReport = RemoteReport("naviReport", userId = naviProfile.uid)
        reportRemoteDatasourceImpl.addReport(naviReport)
        logout()

        val wheretogoProfile = login("wheretogo")
        val wheretogoReport = RemoteReport("wheretogoReport", userId = wheretogoProfile.uid)
        reportRemoteDatasourceImpl.addReport(wheretogoReport)
        reportRemoteDatasourceImpl.removeReport(wheretogoReport.reportId)
        assertThrowsPermission { reportRemoteDatasourceImpl.addReport(naviReport) }
        assertThrowsPermission { reportRemoteDatasourceImpl.removeReport(naviReport.reportId) }
        logout()

        login("navi")
        reportRemoteDatasourceImpl.removeReport(naviReport.reportId)
        logout()

        assertThrowsPermission { reportRemoteDatasourceImpl.addReport(wheretogoReport) }
        assertThrowsPermission { reportRemoteDatasourceImpl.removeReport(wheretogoReport.reportId) }
    }


    private suspend fun login(name: String): AuthProfile {
        val context = getContext()
        CoroutineScope(Dispatchers.IO).launch {
            pickAccount(name)
        }
        val authRequest = googleAuthOnDevice(getGoogleIdOption, context)
        val authResponse =
            authRemoteDatasourceImpl.authGoogleWithFirebase(authRequest!!.authToken!!)
        return authResponse!!
    }

    private suspend fun logout() {
        authRemoteDatasourceImpl.signOutOnFirebase()
    }


    private fun assertThrowsPermission(scope: suspend () -> Unit) {
        assertThrows(FirebaseFirestoreException::class.java) {
            runBlocking {
                scope()
            }
        }.let {
            assertEquals("PERMISSION_DENIED: Missing or insufficient permissions.", it.message)
        }

    }
}