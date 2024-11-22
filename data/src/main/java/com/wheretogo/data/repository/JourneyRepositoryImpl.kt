package com.wheretogo.data.repository

import android.util.Log
import com.wheretogo.data.BuildConfig
import com.wheretogo.data.datasource.database.JourneyDatabase
import com.wheretogo.data.datasource.service.NaverMapApiService
import com.wheretogo.data.model.getCheckpointDummy
import com.wheretogo.data.model.journey.LocalJourney
import com.wheretogo.data.model.toCourse
import com.wheretogo.data.model.toJourney
import com.wheretogo.data.model.toLocalCourse
import com.wheretogo.data.model.toLocalJourney
import com.wheretogo.data.model.toLocalLatlngList
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.domain.repository.CourseRepository
import com.wheretogo.domain.repository.JourneyRepository
import javax.inject.Inject

class JourneyRepositoryImpl @Inject constructor(
    private val pointApiService: NaverMapApiService,
    private val journeyDatabase: JourneyDatabase,
    private val courseRepository: CourseRepository,
) :
    JourneyRepository {

    override suspend fun getJourneys(size: Int): List<Journey> {
        return journeyDatabase.journeyDao().selectAll(size).map { it.toJourney() }
    }

    override suspend fun getJourney(course: Course): Journey {
        return journeyDatabase.journeyDao().select(course.code).run {
            this?.apply {
                if (this.points.isEmpty())
                    journeyDatabase.journeyDao()
                        .insert(this.copy(points = getPoints(course).toLocalLatlngList()))
            }?.toJourney() ?: return LocalJourney(
                code = course.code,
                latitude = course.start.latitude,
                longitude = course.start.longitude,
                course = course.toLocalCourse(),
                pointsDate = System.currentTimeMillis(),
                points = getPoints(course).toLocalLatlngList()
            ).apply {
                journeyDatabase.journeyDao().insert(this)
            }.toJourney()
        }
    }


    override suspend fun getJourney(code: Int): Journey? {
        return journeyDatabase.journeyDao().select(code)?.toJourney()
    }

    override suspend fun getJourneyInViewPort(
        viewPort: Viewport
    ): List<Journey> {
        return viewPort.run {
            journeyDatabase.journeyDao()
                .selectInViewPort(minLatitude, maxLatitude, minLongitude, maxLongitude)
        }.map {
            if (it.points.isEmpty()) {
                it.copy(
                    pointsDate = System.currentTimeMillis(),
                    points = getPoints(it.course.toCourse()).toLocalLatlngList()
                ).apply {
                    journeyDatabase.journeyDao().insert(this)
                }
            } else {
                it
            }.run {
                this.toJourney().copy(checkPoints = getCheckpointDummy(it.code))
            }
        }
    }

    override suspend fun setJourney(map: Journey) {
        journeyDatabase.journeyDao().insert(map.toLocalJourney())
    }


    override suspend fun fetchJourneyWithoutPoints() {
        courseRepository.getCourse().forEach {
            it.let { course ->
                LocalJourney(
                    code = course.code,
                    latitude = course.start.latitude,
                    longitude = course.start.longitude,
                    course = course.toLocalCourse(),
                    pointsDate = 0L,
                    points = emptyList()
                ).apply { journeyDatabase.journeyDao().insert(this) }
            }
        }
    }

    private suspend fun getPoints(course: Course): List<LatLng> {
        val msg = pointApiService.getRouteWayPoint(
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