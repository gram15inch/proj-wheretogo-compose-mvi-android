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
        val addUser = AuthProfile(uid = "addUser1", email = "addUser1@email.com", userName = "add1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = addUser)
        val course = MockModelModule().provideRemoteCourseGroup().first().run {
            this.toCourse(route = waypoints)
        }

        getNearByCourseUseCase(course.cameraLatLng).empty(course.courseId)

        signUpAndSignInUseCase(authRequest).success()
        addCourseUseCase(course).success()

        signOutUseCase().success()
        getNearByCourseUseCase(course.cameraLatLng).contain(course.courseId)

    }

    @Test // 인증된 사용자가 코스를 추가하거나 삭제하기
    fun scenario2(): Unit = runBlocking {
        val addUser = AuthProfile(uid = "addUser1", email = "addUser1@email.com", userName = "add1")
        val authRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = addUser)
        val baseCourse = MockModelModule().provideRemoteCourseGroup().first().run {
            this.toCourse(route = waypoints)
        }
        val addCourse = baseCourse.copy("add1")
        val removeCourse = baseCourse.copy("remove1")

        signUpAndSignInUseCase(authRequest).success()
        getNearByCourseUseCase(removeCourse.cameraLatLng).empty(addCourse.courseId)

        addCourseUseCase(addCourse).success()
        addCourseUseCase(removeCourse).success()
        getNearByCourseUseCase(addCourse.cameraLatLng).contain(addCourse.courseId)
        getNearByCourseUseCase(addCourse.cameraLatLng).contain(removeCourse.courseId)
        getHistoryStreamUseCase().first().courseGroup.contain(addCourse.courseId)
        getHistoryStreamUseCase().first().courseGroup.contain(removeCourse.courseId)

        removeCourseUseCase(removeCourse.courseId).success()
        getNearByCourseUseCase(removeCourse.cameraLatLng).contain(addCourse.courseId)
        getNearByCourseUseCase(removeCourse.cameraLatLng).empty(removeCourse.courseId)
        getHistoryStreamUseCase().first().courseGroup.contain(addCourse.courseId)
        getHistoryStreamUseCase().first().courseGroup.empty(removeCourse.courseId)

        signOutUseCase().success()
        addCourseUseCase(addCourse).fail()
        removeCourseUseCase(removeCourse.courseId).fail()

    }

    @Test // 인증된 사용자가 코스를 신고하기
    fun scenario3(): Unit = runBlocking {
        val baseCourse = getCourseDummy().first().run { copy(points = waypoints) }
        val reportUser = AuthProfile(uid = "report1", email = "report1@email.com", userName = "report1")
        val unknownUser = AuthProfile(uid = "unkonwn1", email = "unkonwn1@email.com", userName = "unkonwn1")
        val reportAuthRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = reportUser)
        val unknownAuthRequest = AuthRequest(authType = AuthType.PROFILE, authProfile = unknownUser)
        val reportCourse = baseCourse.copy(courseId = "reportCourse1", userId = reportUser.uid, checkpointIdGroup = emptyList())
        val normalCourse = baseCourse.copy(courseId = "normalCourse1", userId = unknownUser.uid, checkpointIdGroup = emptyList())

        signUpAndSignInUseCase(reportAuthRequest).success("[로그인] ")
        addCourseUseCase(reportCourse).success()
        addCourseUseCase(normalCourse).success()
        getMyReportUseCase().data!!.empty(reportCourse.courseId)

        val reportId = reportCourseUseCase(reportCourse, "test").success("[신고] ")
        getMyReportUseCase().data!!.contain(reportCourse.courseId)
        getMyReportUseCase().data!!.empty(normalCourse.courseId)
        getHistoryStreamUseCase().first().reportGroup.contain(reportCourse.courseId)
        getNearByCourseUseCase(baseCourse.cameraLatLng).empty(reportCourse.courseId)

        reportCancelUseCase(reportId, "test").success("[신고 취소] ")
        getMyReportUseCase().data!!.empty(reportCourse.courseId)
        getHistoryStreamUseCase().first().reportGroup.empty(reportCourse.courseId)
        getNearByCourseUseCase(baseCourse.cameraLatLng).contain(reportCourse.courseId)

        signOutUseCase().success("[로그아웃] ")
        getNearByCourseUseCase(baseCourse.cameraLatLng).contain(reportCourse.courseId)

        signUpAndSignInUseCase(unknownAuthRequest).success("[로그인] ")
        getMyReportUseCase().data!!.empty(reportCourse.courseId)
        getNearByCourseUseCase(baseCourse.cameraLatLng).contain(reportCourse.courseId)
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

    private fun HashSet<String>.contain(contentId:String){
        Log.d(tag, "contain($contentId): ${contentId in this} / ${this.map { it }}")
        assertTrue(contentId in this)
    }

    private fun HashSet<String>.empty(contentId:String){
        Log.d(tag, "empty($contentId): ${contentId !in this} / ${this.map { it }}")
        assertTrue(contentId !in this)
    }

}