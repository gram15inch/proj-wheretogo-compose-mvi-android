package com.wheretogo.data

import com.wheretogo.data.model.journey.LocalCourse
import com.wheretogo.data.model.journey.LocalJourney
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Journey
import com.wheretogo.domain.model.map.LatLng


fun LocalJourney.toJourney(): Journey {
    return Journey(
        code = this.code,
        course = this.course.toCourse(),
        points = this.points.toLatlngList()
    )
}

fun DataLatLng.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun LocalCourse.toCourse(): Course {
    return Course(
        code = this.code,
        start = this.start.toLatLng(),
        goal = this.goal.toLatLng(),
        waypoints = this.waypoints.toLatlngList()
    )
}

fun List<DataLatLng>.toLatlngList(): List<LatLng> {
    return this.map { it.toLatLng() }
}

fun Journey.toLocalJourney(): LocalJourney {
    return LocalJourney(
        code = code,
        latitude = this.course.start.latitude,
        longitude = this.course.start.longitude,
        course = this.course.toLocalCourse(),
        pointsDate = 0L,
        points = this.points.toLocalLatlngList(),
    )
}

fun LatLng.toLocalLatLng(): DataLatLng {
    return DataLatLng(this.latitude, this.longitude)
}

fun Course.toLocalCourse(): LocalCourse {
    return LocalCourse(
        code = this.code,
        start = this.start.toLocalLatLng(),
        goal = this.goal.toLocalLatLng(),
        waypoints = this.waypoints.toLocalLatlngList()
    )
}

fun List<LatLng>.toLocalLatlngList(): List<DataLatLng> {
    return this.map { it.toLocalLatLng() }
}