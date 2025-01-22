package com.dhkim139.wheretogo.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.MockModelModule
import com.wheretogo.data.toCourse
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.CheckPointAddRequest
import com.wheretogo.domain.model.user.SignInRequest
import com.wheretogo.domain.usecase.community.GetReportUseCase
import com.wheretogo.domain.usecase.community.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.community.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.map.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.GetCheckpointForMarkerUseCase
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
        signInUseCase(SignInRequest(user.token))
        addCourseUseCase(addCourse).success()

        getCheckpointForMarkerUseCase(addCourse.courseId).apply {
            Log.d(tag, "cp: ${this.firstOrNull()}")
            assertEquals(null, this.firstOrNull())
        }

        addCheckpointToCourseUseCase(addCheckPoint).success()
        getCheckpointForMarkerUseCase(addCourse.courseId).apply {
            this.firstOrNull().let {
                Log.d(tag, "cp: ${it}")
                assertNotEquals(null, it)
                if (it != null) {
                    assertEquals(true, File(it.imageLocalPath).exists())
                }
            }
        }
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

        signInUseCase(SignInRequest(user.token))
        addCourseUseCase(addCourse).success()
        addCheckpointToCourseUseCase(reportCheckPoint).success()

        val cp = getCheckpointForMarkerUseCase(reportCheckPoint.courseId).first()
        reportCheckPointUseCase(cp, "test").success()
        getReportUseCase(ReportType.CHECKPOINT).apply {
            Log.d(tag, "rp: ${this.firstOrNull { it.contentId == cp.checkPointId }}")
            assertNotEquals(null, this.firstOrNull { it.contentId == cp.checkPointId })
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

    private fun getAssetFileUri(context: Context, assetFileName: String): Uri? {
        val assetManager = context.assets
        val file = File.createTempFile("test_image", ".jpg_pb")

        try {
            assetManager.open(assetFileName).use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return Uri.fromFile(file)
    }
}