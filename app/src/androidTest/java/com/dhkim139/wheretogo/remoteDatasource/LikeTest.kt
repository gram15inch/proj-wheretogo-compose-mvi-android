package com.dhkim139.wheretogo.remoteDatasource

import com.wheretogo.data.LikeObject
import com.wheretogo.data.datasourceimpl.LikeRemoteDatasourceImpl
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.course.RemoteLike
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import javax.inject.Inject

@HiltAndroidTest
class LikeTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var likeRemoteDatasourceImpl: LikeRemoteDatasourceImpl

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun getAndSetAndRemoveLikeTest(): Unit = runBlocking {
        val datasource = likeRemoteDatasourceImpl
        val cs1 = RemoteCourse(
            courseId = "cs1",
        )
        val l1 = RemoteLike(
            like = 1,
        )

        assertEquals(true, datasource.setLikeInObject(LikeObject.COURSE_LIKE, cs1.courseId, l1))

        val l2 = datasource.getLikeInObject(LikeObject.COURSE_LIKE, cs1.courseId)
        assertEquals(l1, l2)

        assertEquals(true, datasource.removeLikeInCourse(LikeObject.COURSE_LIKE, cs1.courseId))

        val l3 = datasource.getLikeInObject(LikeObject.COURSE_LIKE, cs1.courseId)

        assertEquals(0, l3.like)
    }
}