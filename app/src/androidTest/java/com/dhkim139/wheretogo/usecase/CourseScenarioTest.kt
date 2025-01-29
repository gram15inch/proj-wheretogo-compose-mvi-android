package com.dhkim139.wheretogo.usecase

import android.util.Log
import com.dhkim139.wheretogo.di.MockModelModule
import com.wheretogo.data.toCourse
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.user.AuthData
import com.wheretogo.domain.usecase.community.GetMyReportUseCase
import com.wheretogo.domain.usecase.community.RemoveCourseUseCase
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
    //@Inject lateinit var reportCancelUseCase: ReportCancelUseCase
    //@Inject lateinit var getHistoryStreamUseCase: GetHistoryStreamUseCase
    @Inject lateinit var getMyReportUseCase: GetMyReportUseCase


    @Test // 누구나 위치기반 코스 불러오기
    fun scenario1(): Unit = runBlocking {

        val course = MockModelModule().provideRemoteCourseGroup().first()
        val list = getNearByCourseUseCase(course.cameraLatLng)
        assertEquals(true, list.isNotEmpty())

    }

    @Test // 인증된 사용자가 코스를 추가하거나 삭제하기
    fun scenario2(): Unit = runBlocking {
        val addCourse = MockModelModule().provideRemoteCourseGroup().first().run {
            this.copy("cs999").toCourse(route = waypoints)
        }
        val removeCourse = MockModelModule().provideRemoteCourseGroup().first()

        signInUseCase().success()
        getNearByCourseUseCase(removeCourse.cameraLatLng).empty(addCourse.courseId)

        addCourseUseCase(addCourse).success()
        getNearByCourseUseCase(addCourse.cameraLatLng).contain(addCourse.courseId)
        //getHistoryStreamUseCase().first().courseGroup.contain(addCourse.courseId)

        removeCourseUseCase(removeCourse.courseId).success()
        getNearByCourseUseCase(removeCourse.cameraLatLng).empty(removeCourse.courseId)

        signOutUseCase().success()
        addCourseUseCase(addCourse).fail()
        removeCourseUseCase(removeCourse.courseId).fail()

    }

    @Test // 인증된 사용자가 코스를 신고하기
    fun scenario3(): Unit = runBlocking {
        val reportCourse = MockModelModule().provideRemoteCourseGroup().first()
        val reportUser = AuthData(
            uid = "report1",
            email = "report1@email.com",
            userName = "report1"
        )
        val unknownUser = AuthData(
            uid = "unkonwn1",
            email = "unkonwn1@email.com",
            userName = "unkonwn1"
        )
        signUpAndSignInUseCase(reportUser).success()
        getMyReportUseCase().data!!.empty(reportCourse.courseId)

        reportCourseUseCase(reportCourse.toCourse(), "test").success()
        getMyReportUseCase().data!!.contain(reportCourse.courseId)
        getNearByCourseUseCase(LatLng()).empty(reportCourse.courseId)

    }

    private fun UseCaseResponse<String>.success() {
        this.apply {
            Log.d(tag, "${this::class.simpleName}: ${this.data}")
            assertEquals(UseCaseResponse.Status.Success, this.status)
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
        Log.d(tag, "contain: ${courseId in this.map { it.courseId }} / ${this.map { it.courseId }}")
        assertTrue(courseId in this.map { it.courseId })
    }

    @JvmName("no1")
    private fun List<Course>.empty(courseId: String) {
        Log.d(tag, "empty: ${courseId !in this.map { it.courseId }} / ${this.map { it.courseId }}")
        assertTrue(courseId !in this.map { it.courseId })
    }

    @JvmName("co2")
    private fun List<Report>.contain(courseId: String) {
        Log.d(tag, "contain: ${courseId in this.map { it.contentId }} / ${this.map { it.contentId }}")
        assertTrue(courseId in this.map { it.contentId })
    }

    @JvmName("no2")
    private fun List<Report>.empty(courseId: String) {
        Log.d(tag, "empty: ${courseId !in this.map { it.contentId }} / ${this.map { it.contentId }}")
        assertTrue(courseId !in this.map { it.contentId })
    }

    private fun HashSet<String>.contain(id:String){
        Log.d(tag, "contain: ${id in this} / ${this.map { it }}")
        assertTrue(id in this)
    }

    private fun HashSet<String>.empty(id:String){
        Log.d(tag, "empty: ${id !in this} / ${this.map { it }}")
        assertTrue(id !in this)
    }

}