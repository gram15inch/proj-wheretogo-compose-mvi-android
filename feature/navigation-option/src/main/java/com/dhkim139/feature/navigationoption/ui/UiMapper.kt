package com.dhkim139.feature.navigationoption.ui

import com.dhkim139.feature.navigationoption.EndpointRole
import com.dhkim139.feature.navigationoption.NavLatLng
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.StartDirection

internal fun Course.toNav(direction: StartDirection): NavCourse? {
    val waypoints = if (direction == StartDirection.FORWARD) fWaypoints else bWaypoints
    if (waypoints.size < 2)
        return null
    return NavCourse(
        id = id,
        name = title,
        path = waypoints.toNavGroup(),
        distanceM = 0,
        durationSec = (durationMils.forward * 1000).toInt(),
        start = CourseEndpoint(
            role = EndpointRole.START,
            position = waypoints.first().toNav(),
            addressText = "시작 지점",
        ),
        end = CourseEndpoint(
            role = EndpointRole.END,
            position = waypoints.last().toNav(),
            addressText = "끝 지점",
        )
    )
}

private fun List<LatLng>.toNavGroup(): List<NavLatLng> {
    return map { it.toNav() }
}

private fun LatLng.toNav(): NavLatLng {
    return NavLatLng(latitude, longitude)
}

