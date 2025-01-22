package com.dhkim139.wheretogo.usecase

import android.util.Log
import com.dhkim139.wheretogo.di.MockModelModule
import com.wheretogo.data.toCourse
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.user.SignInRequest
import com.wheretogo.domain.usecase.community.GetReportUseCase
import com.wheretogo.domain.usecase.community.RemoveCourseUseCase
import com.wheretogo.domain.usecase.community.ReportCourseUseCase
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.user.UserSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import jakarta.inject.Inject
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotEquals

@HiltAndroidTest
class CourseScenarioTest {
    val tag = "tst_course"

    @get:Rule var hiltRule = HiltAndroidRule(this)
    @Before fun init() { hiltRule.inject() }

    @Inject lateinit var getNearByCourseUseCase: GetNearByCourseUseCase
    @Inject lateinit var signInUseCase: UserSignInUseCase
    @Inject lateinit var signOutUseCase: UserSignOutUseCase
    @Inject lateinit var addCourseUseCase: AddCourseUseCase
    @Inject lateinit var removeCourseUseCase: RemoveCourseUseCase
    @Inject lateinit var reportCourseUseCase: ReportCourseUseCase
    @Inject lateinit var getReportUseCase: GetReportUseCase

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
        val user = MockModelModule().provideRemoteUser()
        signInUseCase(SignInRequest(user.token)).success()

        getNearByCourseUseCase(removeCourse.cameraLatLng).apply {
            assertEquals(null, this.firstOrNull { it.courseId == addCourse.courseId })
            Log.d(tag, "${this.firstOrNull { it.courseId == addCourse.courseId }}")
        }

        addCourseUseCase(addCourse).success()

        getNearByCourseUseCase(removeCourse.cameraLatLng).apply {
            Log.d(tag, "${this.firstOrNull { it.courseId == addCourse.courseId }}")
            assertNotEquals(null, this.firstOrNull { it.courseId == addCourse.courseId })
        }

        removeCourseUseCase(removeCourse.courseId).success()

        getNearByCourseUseCase(removeCourse.cameraLatLng).apply {
            Log.d(tag, "${this.firstOrNull { it.courseId == removeCourse.courseId }}")
            assertEquals(null, this.firstOrNull { it.courseId == removeCourse.courseId })
        }

        signOutUseCase().success()

        removeCourseUseCase(removeCourse.courseId).fail()

    }

    @Test // 인증된 사용자가 코스를 신고하기
    fun scenario3(): Unit = runBlocking {
        val reportCourse = MockModelModule().provideRemoteCourseGroup().first()
        val user = MockModelModule().provideRemoteUser()
        signInUseCase(SignInRequest(user.token)).success()

        getReportUseCase(ReportType.COURSE).apply {
            assertEquals(null, this.firstOrNull { it.contentId == reportCourse.courseId })
            Log.d(tag, "${this.firstOrNull { it.contentId == reportCourse.courseId }}")
        }

        reportCourseUseCase(reportCourse.toCourse(), "test").success()

        getReportUseCase(ReportType.COURSE).apply {
            Log.d(tag, "${this.firstOrNull { it.contentId == reportCourse.courseId }}")
            assertNotEquals(null, this.firstOrNull { it.contentId == reportCourse.courseId })
        }
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
}