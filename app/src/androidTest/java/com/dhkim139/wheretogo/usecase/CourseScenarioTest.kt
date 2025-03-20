package com.dhkim139.wheretogo.usecase

import android.util.Log
import com.dhkim139.wheretogo.di.MockModelModule
import com.wheretogo.data.toCourse
import com.wheretogo.domain.AuthType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.auth.AuthRequest
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.toCourseAddRequest
import com.wheretogo.domain.usecase.community.GetMyReportUseCase
import com.wheretogo.domain.usecase.community.RemoveCourseUseCase
import com.wheretogo.domain.usecase.community.ReportCancelUseCase
import com.wheretogo.domain.usecase.community.ReportCourseUseCase
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
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
import org.junit.jupiter.api.Assertions.assertTrue

@HiltAndroidTest
class CourseScenarioTest {
    val tag = "tst_course"

    @get:Rule var hiltRule = HiltAndroidRule(this)
    @Before fun init() { hiltRule.inject() }

    @Inject lateinit var getNearByCourseUseCase: GetNearByCourseUseCase
    @Inject lateinit var signInUseCase: UserSignInUseCase
    @Inject lateinit var signUpAndSignInUseCase: UserSignUpAndSignInUseCase
    @Inject lateinit var signOutUseCase: UserSignOutUseCase
    @Inject lateinit var addCourseUseCase: AddCourseUseCase
    @Inject lateinit var removeCourseUseCase: RemoveCourseUseCase
    @Inject lateinit var reportCourseUseCase: ReportCourseUseCase
    @Inject lateinit var reportCancelUseCase: ReportCancelUseCase
    @Inject lateinit var getHistoryStreamUseCase: GetHistoryStreamUseCase
    @Inject lateinit var getMyReportUseCase: GetMyReportUseCase


    @Test // 누구나 위치기반 코스 불러오기
    fun scenario1(): Unit = runBlocking {
        val addUser = AuthProfile(uid = "addUser1", email = "addUse  r1@email.com", userName = "add1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = addUser)
        val inputCourse = MockModelModule().provideRemoteCourseGroup().first().run {
            this.toCourse(points = waypoints)
        }

        getNearByCourseUseCase(inputCourse.cameraLatLng, 8.0).empty(inputCourse.courseId)

        signUpAndSignInUseCase(authRequest).success()
        val outputCourseId = addCourseUseCase(inputCourse.toCourseAddRequest()).success()

        signOutUseCase().success()
        getNearByCourseUseCase(inputCourse.cameraLatLng, 8.0).contain(outputCourseId)

    }

    @Test // 인증된 사용자가 코스를 추가하거나 삭제하기
    fun scenario2(): Unit = runBlocking {
        val addUser = AuthProfile(uid = "addUser1", email = "addUser1@email.com", userName = "add1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = addUser)
        val inputCourse = MockModelModule().provideRemoteCourseGroup().first().run {
            this.toCourse(points = waypoints)
        }

        signUpAndSignInUseCase(authRequest).success()

        val outputCourseId = addCourseUseCase(inputCourse.toCourseAddRequest()).success()
        val outputRemoveCourseId = addCourseUseCase(inputCourse.toCourseAddRequest()).success()
        getNearByCourseUseCase(inputCourse.cameraLatLng, 8.0).contain(outputCourseId)
        getNearByCourseUseCase(inputCourse.cameraLatLng, 8.0).contain(outputRemoveCourseId)
        getNearByCourseUseCase(inputCourse.cameraLatLng, 9.5).assertEquals(inputCourse.copy(courseId = outputCourseId))
        getNearByCourseUseCase(inputCourse.cameraLatLng, 9.0).assertEquals(inputCourse.copy(courseId = outputCourseId, points = emptyList()))
        getHistoryStreamUseCase().first().courseGroup.contain(outputCourseId)
        getHistoryStreamUseCase().first().courseGroup.contain(outputRemoveCourseId)

        removeCourseUseCase(outputRemoveCourseId).success()
        getNearByCourseUseCase(inputCourse.cameraLatLng, 8.0).contain(outputCourseId)
        getNearByCourseUseCase(inputCourse.cameraLatLng, 8.0).empty(outputRemoveCourseId)
        getHistoryStreamUseCase().first().courseGroup.contain(outputCourseId)
        getHistoryStreamUseCase().first().courseGroup.empty(outputRemoveCourseId)

        signOutUseCase().success()
        addCourseUseCase(inputCourse.toCourseAddRequest()).fail()
        removeCourseUseCase(outputRemoveCourseId).fail()

    }

    @Test // 인증된 사용자가 코스를 신고하기
    fun scenario3(): Unit = runBlocking {
        val baseCourse = getCourseDummy().first().run { copy(points = waypoints) }
        val reportUser = AuthProfile(uid = "report1", email = "report1@email.com", userName = "report1")
        val unknownUser = AuthProfile(uid = "unkonwn1", email = "unkonwn1@email.com", userName = "unkonwn1")
        val reportAuthRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = reportUser)
        val unknownAuthRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = unknownUser)

        //로그인후 코스추가
        signUpAndSignInUseCase(reportAuthRequest).success("[로그인] ")
        val inputCourseId1= addCourseUseCase(baseCourse.toCourseAddRequest()).success()
        val inputCourseId2= addCourseUseCase(baseCourse.toCourseAddRequest()).success()
        val outputCourse1 = getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).first { it.courseId==inputCourseId1 }
        val outputCourse2 = getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).first { it.courseId==inputCourseId2 }


        //신고한 코스 숨기기
        val reportId1 = reportCourseUseCase(outputCourse1, "test").success("[신고] 1")
        val reportId2 = reportCourseUseCase(outputCourse2, "test").success("[신고] 2")
        getMyReportUseCase().data!!.contain(inputCourseId1)
        getMyReportUseCase().data!!.contain(inputCourseId2)
        getHistoryStreamUseCase().first().reportGroup.contain(inputCourseId1)
        getHistoryStreamUseCase().first().reportGroup.contain(inputCourseId2)
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).empty(inputCourseId1)
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).empty(inputCourseId2)

        // 신고취소한 코스 보이기
        reportCancelUseCase(reportId1, "test").success("[신고 취소] ")
        getMyReportUseCase().data!!.empty(inputCourseId1)
        getMyReportUseCase().data!!.contain(inputCourseId2)
        getHistoryStreamUseCase().first().reportGroup.empty(inputCourseId1)
        getHistoryStreamUseCase().first().reportGroup.contain(inputCourseId2)
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).contain(inputCourseId1)
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).empty(inputCourseId2)

        //로그아웃 후에 모든 코스 보이기
        signOutUseCase().success("[로그아웃] ")
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).contain(inputCourseId1)
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).contain(inputCourseId2)

        //신고하지 않은 사용자 로그인후 모든 코스 보이기
        signUpAndSignInUseCase(unknownAuthRequest).success("[로그인] ")
        getMyReportUseCase().data!!.empty(reportId1)
        getMyReportUseCase().data!!.empty(reportId2)
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).contain(inputCourseId1)
        getNearByCourseUseCase(baseCourse.cameraLatLng, 8.0).contain(inputCourseId2)
    }

    private fun UseCaseResponse<String>.success(msg:String=""):String {
       return this.run {
            Log.d(tag, "$msg${this}")
            assertEquals(UseCaseResponse.Status.Success, this.status)
            this.data?:""
        }
    }

    private fun UseCaseResponse<String>.fail() {
        this.apply {
            Log.d(tag, "${this::class.simpleName}: ${this}")
            assertEquals(UseCaseResponse.Status.Fail, this.status)
        }
    }

    @JvmName("co1")
    private fun List<Course>.contain(courseId: String) {
        Log.d(tag, "contain($courseId): ${courseId in this.map { it.courseId }} / ${this.map { it.courseId }}")
        assertTrue(courseId in this.map { it.courseId })
    }

    @JvmName("no1")
    private fun List<Course>.empty(courseId: String) {
        Log.d(tag, "empty($courseId): ${courseId !in this.map { it.courseId }} / ${this.map { it.courseId }}")
        assertTrue(courseId !in this.map { it.courseId })
    }

    @JvmName("co2")
    private fun List<Report>.contain(courseId: String) {
        Log.d(tag, "contain($courseId): ${courseId in this.map { it.contentId }} / ${this.map { it.contentId }}")
        assertTrue(courseId in this.map { it.contentId })
    }

    @JvmName("no2")
    private fun List<Report>.empty(courseId: String) {
        Log.d(tag, "empty($courseId): ${courseId !in this.map { it.contentId }} / ${this.map { it.contentId }}")
        assertTrue(courseId !in this.map { it.contentId })
    }

    @JvmName("co3")
    private fun List<Course>.assertEquals(course:Course){
        val matchedCourse = firstOrNull { it.courseId == course.courseId }!!
        Log.d(tag, "contain(${course.courseId}): ${matchedCourse.points.size} / ${course.waypoints.size}}")
        assertEquals(course.courseName, matchedCourse.courseName)
        assertEquals(course.waypoints, matchedCourse.waypoints)
        assertEquals(course.points, matchedCourse.points)
    }

    private fun HashSet<String>.contain(contentId:String){
        Log.d(tag, "contain($contentId): ${contentId in this} / ${this.map { it }}")
        assertTrue(contentId in this)
    }

    private fun HashSet<String>.empty(contentId:String){
        Log.d(tag, "empty($contentId): ${contentId !in this} / ${this.map { it }}")
        assertTrue(contentId !in this)
    }

}