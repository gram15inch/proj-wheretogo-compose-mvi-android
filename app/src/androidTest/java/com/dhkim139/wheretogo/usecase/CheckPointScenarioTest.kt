package com.dhkim139.wheretogo.usecase

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.MockModelModule
import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.toCourse
import com.wheretogo.domain.AuthType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.CheckPointAddRequest
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.toCourseAddRequest
import com.wheretogo.domain.usecase.community.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetMyReportUseCase
import com.wheretogo.domain.usecase.community.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.community.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.map.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.presentation.feature.getAssetFileUri
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import jakarta.inject.Inject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue

@HiltAndroidTest
class CheckPointScenarioTest {
    val tag = "tst_checkpoint"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var user: MockRemoteUser
    @Inject
    lateinit var signInUseCase: UserSignInUseCase
    @Inject
    lateinit var signUpAndSignInUseCase: UserSignUpAndSignInUseCase
    @Inject
    lateinit var signOutUseCase: UserSignOutUseCase
    @Inject
    lateinit var addCourseUseCase: AddCourseUseCase
    @Inject
    lateinit var addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase
    @Inject
    lateinit var addCommentToCheckPointUseCase: AddCommentToCheckPointUseCase
    @Inject
    lateinit var removeCheckPointUseCase: RemoveCheckPointUseCase
    @Inject
    lateinit var reportCheckPointUseCase: ReportCheckPointUseCase
    @Inject
    lateinit var getCheckpointForMarkerUseCase: GetCheckpointForMarkerUseCase
    @Inject
    lateinit var getCommentForCheckPointUseCase: GetCommentForCheckPointUseCase
    @Inject
    lateinit var getHistoryStreamUseCase: GetHistoryStreamUseCase
    @Inject
    lateinit var getMyReportUseCase: GetMyReportUseCase

    @Test // 인증된 사용자가 체크포인트를 추가하거나 삭제하기
    fun scenario1(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val addUser = AuthProfile(uid = "addUser1", email = "addUser1@email.com", userName = "add1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = addUser)
        val addCourse = MockModelModule().provideRemoteCourseGroup()
            .first { it.courseId == "cs3" }
            .run { toCourse(points = waypoints).toCourseAddRequest() }
        val uri = getAssetFileUri(context, "photo_opt.jpg")

        //로그인후 코스, 체크포인트 추가
        signUpAndSignInUseCase(authRequest).success()
        val inputCourseId = addCourseUseCase(addCourse).success()
        val inputCheckPointAddRequest = CheckPointAddRequest(
            courseId = inputCourseId,
            latLng = addCourse.waypoints.first(),
            imageUri = uri,
            description = "description1"
        )
        val outputCheckPointId = addCheckpointToCourseUseCase(inputCheckPointAddRequest).success()
        val inputComment = Comment(
            commentId = "cm1",
            groupId = outputCheckPointId
        )
        addCommentToCheckPointUseCase(inputComment).success()
        getCheckpointForMarkerUseCase(inputCourseId).contain(outputCheckPointId)
        getCommentForCheckPointUseCase(outputCheckPointId).notEmpty()
        getHistoryStreamUseCase().first().checkpointGroup.contain(outputCheckPointId)

        //체크포인트 삭제
        removeCheckPointUseCase(inputCourseId, outputCheckPointId).success()
        getHistoryStreamUseCase().first().checkpointGroup.empty(outputCheckPointId)
        getCommentForCheckPointUseCase(outputCheckPointId).empty()
        getCheckpointForMarkerUseCase(inputCourseId).empty(outputCheckPointId)
    }

    @Test // 인증된 사용자가 체크포인트를 신고하기
    fun scenario2(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = getAssetFileUri(context, "photo_opt.jpg")
        val reportUser =
            AuthProfile(uid = "report1", email = "report1@email.com", userName = "report1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = reportUser)
        val baseCourse = getCourseDummy().first().toCourseAddRequest()


        //로그인후 코스, 체크포인트 추가
        signUpAndSignInUseCase(authRequest).success()
        val inputCourseId = addCourseUseCase(baseCourse).success()
        val reportCheckPointAdd = CheckPointAddRequest(
            courseId = inputCourseId,
            latLng = LatLng(1.0, 1.0),
            imageUri = uri,
            description = "description1"
        )
        val normalCheckPointAdd = CheckPointAddRequest(
            courseId = inputCourseId,
            latLng = LatLng(1.1, 1.1),
            imageUri = uri,
            description = "description2"
        )
        val reportCpId = addCheckpointToCourseUseCase(reportCheckPointAdd).success()
        val normalCpId = addCheckpointToCourseUseCase(normalCheckPointAdd).success()

        //체크포인트 신고후 숨기기
        reportCheckPointUseCase(reportCpId, "test")
        getMyReportUseCase().data!!.contain(reportCpId)
        getMyReportUseCase().data!!.empty(normalCpId)
        getCheckpointForMarkerUseCase(inputCourseId).empty(reportCpId)
        getCheckpointForMarkerUseCase(inputCourseId).contain(normalCpId)

        //로그아웃후 모든 체크포인트 보이기
        signOutUseCase().success()
        getCheckpointForMarkerUseCase(inputCourseId).contain(reportCpId)
        getCheckpointForMarkerUseCase(inputCourseId).contain(normalCpId)
    }


    private fun UseCaseResponse<String>.success(): String {
        return this.run {
            Log.d(tag, "${this}")
            assertEquals(UseCaseResponse.Status.Success, this.status)
            this.data ?: ""
        }
    }

    private fun UseCaseResponse<String>.fail() {
        this.apply {
            Log.d(tag, "${this}")
            assertEquals(UseCaseResponse.Status.Fail, this.status)
        }
    }

    @JvmName("co1")
    private fun List<CheckPoint>.contain(checkpointId: String) {
        Log.d(
            tag,
            "contain($checkpointId): ${checkpointId in this.map { it.checkPointId }} / ${this.map { it.checkPointId }}"
        )
        assertTrue(checkpointId in this.map { it.checkPointId })
    }

    @JvmName("no1")
    private fun List<CheckPoint>.empty(checkpointId: String) {
        Log.d(
            tag,
            "empty($checkpointId): ${checkpointId !in this.map { it.checkPointId }} / ${this.map { it.checkPointId }}"
        )
        assertTrue(checkpointId !in this.map { it.checkPointId })
    }


    @JvmName("co2")
    private fun List<Report>.contain(checkPointId: String) {
        Log.d(tag, "contain: ${this.firstOrNull { it.contentId == checkPointId }} / ${this}")
        assertNotEquals(null, this.firstOrNull { it.contentId == checkPointId })
    }

    @JvmName("no2")
    private fun List<Report>.empty(checkPointId: String) {
        Log.d(tag, "empty: ${this.firstOrNull { it.contentId == checkPointId }} / ${this}")
        assertEquals(null, this.firstOrNull { it.contentId == checkPointId })
    }

    private fun HashSet<String>.contain(id: String) {
        Log.d(tag, "contain: ${id in this} / ${this}")
        assertTrue(id in this)
    }

    private fun HashSet<String>.empty(id: String) {
        Log.d(tag, "empty: ${id !in this} / ${this}")
        assertFalse(id in this)
    }

    private fun List<Comment>.empty(){
        Log.d(tag, "empty: / ${this}")
        assertTrue(this.isEmpty())
    }

    private fun List<Comment>.notEmpty(){
        Log.d(tag, "empty: / ${this}")
        assertTrue(this.isNotEmpty())
    }
}