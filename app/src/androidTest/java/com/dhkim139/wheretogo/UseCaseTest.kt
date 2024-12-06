package com.dhkim139.wheretogo

import androidx.test.platform.app.InstrumentationRegistry
import com.dhkim139.wheretogo.di.FirebaseModule
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.LikeRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.di.ApiServiceModule
import com.wheretogo.data.di.DaoDatabaseModule
import com.wheretogo.data.di.RetrofitClientModule
import com.wheretogo.data.repositoryimpl.CourseRepositoryImpl
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.usecaseimpl.GetNearByCourseUseCaseImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// usecaseImpl은 git에 업로드 되지 않음
class UseCaseTest {

    @Test
    fun getNearByJourneyUseCaseTest(): Unit = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val firestore = FirebaseModule.provideFirestore()
        val naverApi = ApiServiceModule.provideNaverMapApiService(
            RetrofitClientModule.run { provideRetrofit(provideMoshi(), provideClient()) })
        val courseDao =
            DaoDatabaseModule.run { provideCourseDao(provideCourseDatabase(appContext)) }

        val courseRemote = CourseRemoteDatasourceImpl(firestore)
        val courseLocal = CourseLocalDatasourceImpl(courseDao)
        val routeRemote = RouteRemoteDatasourceImpl(firestore, naverApi)
        val likeRemote = LikeRemoteDatasourceImpl(firestore)
        val checkPointRemote = CheckPointRemoteDatasourceImpl(firestore)
        val checkPointLocal = CheckPointLocalDatasourceImpl()

        val courseRepository = CourseRepositoryImpl(
            courseRemoteDatasource = courseRemote,
            courseLocalDatasource = courseLocal,
            routeRemoteDatasource = routeRemote,
            likeRemoteDatasource = likeRemote,
            checkPointRemoteDatasource = checkPointRemote,
            checkPointLocalDatasource = checkPointLocal
        )
        val location = LatLng(37.2755481129516, 127.11608496870285)

        val list = GetNearByCourseUseCaseImpl(courseRepository)(location)
        assertEquals(true, list.isNotEmpty())
        assertEquals("[cs2, cs3, cs4, cs5, cs6, cs7]", list.map { it.courseId }.sorted().toString())
    }


}