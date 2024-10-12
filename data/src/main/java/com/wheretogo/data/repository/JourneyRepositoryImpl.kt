package com.wheretogo.data.repository

import android.util.Log
import com.wheretogo.data.BuildConfig
import com.wheretogo.data.datasource.database.JourneyDatabase
import com.wheretogo.data.datasource.service.NaverMapApiService
import com.wheretogo.data.model.journey.LocalJourney
import com.wheretogo.data.model.toJourney
import com.wheretogo.data.model.toLocalCourse
import com.wheretogo.data.model.toLocalJourney
import com.wheretogo.data.model.toLocalLatlngList

import com.wheretogo.domain.model.Course
import com.wheretogo.domain.model.Journey

import com.wheretogo.domain.model.LatLng
import com.wheretogo.domain.model.Viewport
import com.wheretogo.domain.repository.JourneyRepository

import javax.inject.Inject

class JourneyRepositoryImpl @Inject constructor(
    private val mapApiService: NaverMapApiService,
    private val mapDatabase: JourneyDatabase
) :
    JourneyRepository {

    override suspend fun getJourneys(): List<Journey> {
        return mapDatabase.journeyDao().selectAll().map { it.toJourney() }
    }

    override suspend fun getJourney(course: Course): Journey {
        return mapDatabase.journeyDao().select(course.code).run {
            this?.toJourney() ?: return LocalJourney(//todo id 제거 (없음)
                code = course.code,
                latitude = course.start.latitude,
                longitude = course.start.longitude,
                course = course.toLocalCourse(),
                points = getPoints(course).toLocalLatlngList()
            ).apply {
                mapDatabase.journeyDao().insert(this)
            }.toJourney()
        }
    }

    override suspend fun getJourneyInViewPort(
        viewPort: Viewport
    ): List<Journey> {
        return viewPort.run {
            mapDatabase.journeyDao()
                .selectInViewPort(minLatitude, maxLatitude, minLongitude, maxLongitude)
        }.map { it.toJourney() } //todo 서비스api 추가
    }

    override suspend fun setJourney(map: Journey) {
        mapDatabase.journeyDao().insert(map.toLocalJourney())
    }

    private suspend fun getPoints(course: Course): List<LatLng> {
        val msg = mapApiService.getRouteWayPoint(
            BuildConfig.NAVER_CLIENT_ID_KEY,
            BuildConfig.NAVER_CLIENT_SECRET_KEY,
            start = convertLatLng(course.start),
            goal = convertLatLng(course.goal),
            waypoints = convertWaypoints(course.waypoints)
        )

        if (msg.body()?.currentDateTime != null) {
            val r =
                msg.body()!!.route.traoptimal.map { it.path }.first().map { LatLng(it[1], it[0]) }
            return r
        } else {
            Log.d("tst", "${msg}")
            Log.d("tst", "${msg.body()}")
            return listOf()
        }
    }


    private fun convertLatLng(latlng: LatLng): String = "${latlng.longitude}, ${latlng.latitude}"
    private fun convertWaypoints(waypoints: List<LatLng>): String {
        var str = ""
        waypoints.forEach {
            str += convertLatLng(it) + "|"
        }
        return str
    }
}