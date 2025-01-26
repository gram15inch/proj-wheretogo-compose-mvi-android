package com.dhkim139.wheretogo.usecase

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.MockModelModule
import com.wheretogo.data.toCourse
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.CheckPointAddRequest
import com.wheretogo.domain.usecase.community.GetReportUseCase
import com.wheretogo.domain.usecase.community.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.community.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.map.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.user.UserSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.presentation.feature.getAssetFileUri
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import jakarta.inject.Inject
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotEquals
import java.io.File

@HiltAndroidTest
class CheckPointScenarioTest {
    val tag = "tst_checkpoint"

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject lateinit var signInUseCase: UserSignInUseCase
    @Inject lateinit var signOutUseCase: UserSignOutUseCase
    @Inject lateinit var addCourseUseCase: AddCourseUseCase
    @Inject lateinit var addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase
    @Inject lateinit var removeCheckPointUseCase: RemoveCheckPointUseCase
    @Inject lateinit var reportCheckPointUseCase: ReportCheckPointUseCase
    @Inject lateinit var getCheckpointForMarkerUseCase: GetCheckpointForMarkerUseCase
    @Inject lateinit var getReportUseCase: GetReportUseCase

    @Test // 인증된 사용자가 체크포인트를 추가하거나 삭제하기
    fun scenario1(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val user = MockModelModule().provideRemoteUser()
        val addCourse = MockModelModule().provideRemoteCourseGroup()
            .first { it.courseId == "cs3" }
            .run { this.copy("cs999").toCourse(route = waypoints) }
        val uri = getAssetFileUri(context, "photo_opt.jpg")
        val addCheckPoint = CheckPointAddRequest(
            courseId = addCourse.courseId,
            checkpointIdGroup = addCourse.checkpointIdGroup,
            latLng = addCourse.waypoints.first(),
            imageName = "imgName1",
            imageUri = uri,
            description = "description1"
        )
        signInUseCase().success()
        addCourseUseCase(addCourse).success()
        getCheckpointForMarkerUseCase(addCourse.courseId).empty()

        addCheckpointToCourseUseCase(addCheckPoint).success()
        val cp = getCheckpointForMarkerUseCase(addCourse.courseId).first()
        getCheckpointForMarkerUseCase(addCourse.courseId).exist()

        removeCheckPointUseCase(addCourse.courseId, cp.checkPointId).success()
        getCheckpointForMarkerUseCase(addCourse.courseId).empty()
    }

    @Test // 인증된 사용자가 체크포인트를 신고하기
    fun scenario2(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val user = MockModelModule().provideRemoteUser()
        val addCourse = MockModelModule().provideRemoteCourseGroup()
            .first { it.courseId == "cs3" }
            .run { this.copy("cs999").toCourse(route = waypoints) }
        val uri = getAssetFileUri(context, "photo_opt.jpg")
        val reportCheckPoint = CheckPointAddRequest(
            courseId = addCourse.courseId,
            checkpointIdGroup = addCourse.checkpointIdGroup,
            latLng = addCourse.waypoints.first(),
            imageName = "imgName1",
            imageUri = uri,
            description = "description1"
        )

        signInUseCase().success()
        addCourseUseCase(addCourse).success()
        addCheckpointToCourseUseCase(reportCheckPoint).success()
        getCheckpointForMarkerUseCase(reportCheckPoint.courseId).exist()

        val cp = getCheckpointForMarkerUseCase(reportCheckPoint.courseId).first()
        reportCheckPointUseCase(cp, "test").success()
        getReportUseCase(ReportType.CHECKPOINT).contain(cp.checkPointId)
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

    @JvmName("co1")
    private fun List<CheckPoint>.exist() {
        this.firstOrNull().let {
            Log.d(tag, "exist: ${it}")
            assertNotEquals(null, it)
            if (it != null) {
                assertEquals(true, File(it.imageLocalPath).exists())
            }
        }
    }

    @JvmName("no1")
    private fun List<CheckPoint>.empty() {
        Log.d(tag, "empty: ${this.firstOrNull()}")
        assertEquals(null, this.firstOrNull())
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


}