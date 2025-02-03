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
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.user.AuthProfile
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

    @Inject lateinit var user: MockRemoteUser
    @Inject lateinit var signInUseCase: UserSignInUseCase
    @Inject lateinit var signUpAndSignInUseCase: UserSignUpAndSignInUseCase
    @Inject lateinit var signOutUseCase: UserSignOutUseCase
    @Inject lateinit var addCourseUseCase: AddCourseUseCase
    @Inject lateinit var addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase
    @Inject lateinit var removeCheckPointUseCase: RemoveCheckPointUseCase
    @Inject lateinit var reportCheckPointUseCase: ReportCheckPointUseCase
    @Inject lateinit var getCheckpointForMarkerUseCase: GetCheckpointForMarkerUseCase
    @Inject lateinit var getHistoryStreamUseCase: GetHistoryStreamUseCase
    @Inject lateinit var getMyReportUseCase: GetMyReportUseCase

    @Test // 인증된 사용자가 체크포인트를 추가하거나 삭제하기
    fun scenario1(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val addUser = AuthProfile(uid = "addUser1", email = "addUser1@email.com", userName = "add1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = addUser)
        val addCourse = MockModelModule().provideRemoteCourseGroup()
            .first { it.courseId == "cs3" }
            .run { this.copy("cs999").toCourse(route = waypoints) }
        val uri = getAssetFileUri(context, "photo_opt.jpg")
        val addCheckPoint = CheckPointAddRequest(
            courseId = addCourse.courseId,
            latLng = addCourse.waypoints.first(),
            imageName = "imgName1",
            imageUri = uri,
            description = "description1"
        )
        signUpAndSignInUseCase(authRequest).success()
        addCourseUseCase(addCourse).success()

        val cp1 = addCheckpointToCourseUseCase(addCheckPoint).success()
        getCheckpointForMarkerUseCase(addCourse.courseId).contain(cp1)
        getHistoryStreamUseCase().first().checkpointGroup.contain(cp1)

        removeCheckPointUseCase(addCourse.courseId, cp1).success()
        getHistoryStreamUseCase().first().checkpointGroup.empty(cp1)
        getCheckpointForMarkerUseCase(addCourse.courseId).empty(cp1)
    }

    @Test // 인증된 사용자가 체크포인트를 신고하기
    fun scenario2(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val uri = getAssetFileUri(context, "photo_opt.jpg")
        val reportUser = AuthProfile(uid = "report1", email = "report1@email.com", userName = "report1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = reportUser)
        val baseCourse = getCourseDummy().first().run { copy(courseId="cs_cp1", userId= reportUser.uid, points = waypoints) }
        val reportCheckPointAdd = CheckPointAddRequest(
            courseId = baseCourse.courseId,
            latLng = LatLng(1.0,1.0),
            imageName = "imgName1",
            imageUri = uri,
            description = "description1"
        )
        val normalCheckPointAdd = CheckPointAddRequest(
            courseId = baseCourse.courseId,
            latLng = LatLng(1.1,1.1),
            imageName = "imgName2",
            imageUri = uri,
            description = "description2"
        )

        signUpAndSignInUseCase(authRequest).success()
        addCourseUseCase(baseCourse).success()
        val reportCpId = addCheckpointToCourseUseCase(reportCheckPointAdd).success()
        val normalCpId = addCheckpointToCourseUseCase(normalCheckPointAdd).success()

        reportCheckPointUseCase(reportCpId, "test")
        getMyReportUseCase().data!!.contain(reportCpId)
        getMyReportUseCase().data!!.empty(normalCpId)
        getCheckpointForMarkerUseCase(baseCourse.courseId).empty(reportCpId)
        getCheckpointForMarkerUseCase(baseCourse.courseId).contain(normalCpId)

        signOutUseCase().success()
        getCheckpointForMarkerUseCase(baseCourse.courseId).contain(reportCpId)
    }


    private fun UseCaseResponse<String>.success():String {
        return this.run {
            Log.d(tag, "${this}")
            assertEquals(UseCaseResponse.Status.Success, this.status)
            this.data?:""
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
        Log.d(tag, "contain($checkpointId): ${checkpointId in this.map { it.checkPointId }} / ${this.map { it.checkPointId }}")
        assertTrue(checkpointId in this.map { it.checkPointId })
    }

    @JvmName("no1")
    private fun List<CheckPoint>.empty(checkpointId: String) {
        Log.d(tag, "empty($checkpointId): ${checkpointId !in this.map { it.checkPointId }} / ${this.map { it.checkPointId }}")
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

    private fun HashSet<String>.contain(id:String){
        Log.d(tag, "contain: ${id in this} / ${this}")
        assertTrue(id in this)
    }

    private fun HashSet<String>.empty(id:String){
        Log.d(tag, "empty: ${id !in this} / ${this}")
        assertFalse(id in this)
    }

}