package com.dhkim139.wheretogo

import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import jakarta.inject.Inject
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// usecaseImpl은 git에 업로드 되지 않음
@HiltAndroidTest
class UseCaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var getNearByCourseUseCase: GetNearByCourseUseCase

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun getNearByJourneyUseCaseTest(): Unit = runBlocking {
        val location = LatLng(37.2755481129516, 127.11608496870285)
        val list = getNearByCourseUseCase(location)
        assertEquals(true, list.isNotEmpty())
        assertEquals("[cs2, cs3, cs4, cs5, cs6, cs7]", list.map { it.courseId }.sorted().toString())
    }
}