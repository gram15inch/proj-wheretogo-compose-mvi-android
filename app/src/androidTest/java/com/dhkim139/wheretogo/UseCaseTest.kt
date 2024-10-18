package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.google.firebase.FirebaseApp
import com.wheretogo.data.datasource.service.NaverMapApiService
import com.wheretogo.data.di.DaoDatabaseModule
import com.wheretogo.data.di.RetrofitClientModule
import com.wheretogo.data.repository.CourseRepositoryImpl
import com.wheretogo.data.repository.JourneyRepositoryImpl
import com.wheretogo.domain.usecaseimpl.FetchJourneyWithoutPointsUseCaseImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

// usecaseImpl은 git에 업로드 되지 않음
class UseCaseTest {

    @Test
    fun fetchJourneyWithoutPointsUseCaseTest(): Unit = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val firestore = FirebaseModule.provideFirestore().apply { FirebaseApp.initializeApp(appContext) }
        val room = DaoDatabaseModule.provideMapDatabase(appContext)
        val naverMapApiService = RetrofitClientModule.run {
            provideRetrofit(provideMoshi(), provideClient()).create(NaverMapApiService::class.java)
        }

        FetchJourneyWithoutPointsUseCaseImpl(
            JourneyRepositoryImpl(
                naverMapApiService,
                room,
                CourseRepositoryImpl(firestore)
            )
        )()
        val actual = room.journeyDao().selectAll(10)
        assertNotEquals(0, actual.size)
        actual.forEach {
            assertEquals(0, it.points.size)
            assertEquals(0L, it.pointsDate)
            assertNotEquals(-1, it.code)
        }
    }


}