package com.wheretogo.data.course

import com.wheretogo.data.model.course.BoundsColumns
import com.wheretogo.data.model.course.BoundsDto
import com.wheretogo.data.model.course.CourseDto
import com.wheretogo.data.model.course.CourseEntity
import com.wheretogo.data.model.course.LatLngColumns
import com.wheretogo.data.model.course.LatLngDto
import com.wheretogo.data.model.course.LegInt
import com.wheretogo.data.model.course.LegLong
import com.wheretogo.data.model.course.PointsDto
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.GeoBounds
import com.wheretogo.domain.model.course.GeoPoint
import com.wheretogo.domain.model.course.Leg

internal fun CourseDto.toEntities(onDropped: (String) -> Unit = {}): CourseEntity? {
    val id = id ?: return dropped("id 없음", onDropped)
    val routeId = routeId ?: return dropped("$id: routeId 없음", onDropped)
    val points = points ?: return dropped("$id: points 없음", onDropped)
    val title = title ?: return dropped("$id: title 없음", onDropped)
    val uid = uid ?: return dropped("$id: uid 없음", onDropped)
    val type = type ?: return dropped("$id: type 없음", onDropped)
    val level = level ?: return dropped("$id: level 없음", onDropped)
    val center = center.toPoint() ?: return dropped("$id: center 없음", onDropped)
    val bounds = bounds.toBoundsColumns() ?: return dropped("$id: bounds 없거나 뒤집힘", onDropped)
    val updateAt = updateAt ?: return dropped("$id: updateAt 없음", onDropped)
    val createAt = createdAt ?: return dropped("$id: createAt 없음", onDropped)
    val waypoints = points.toLatLngColumns() ?: return dropped("$id: waypoints 없음", onDropped)
    return CourseEntity(
        id = id,
        title = title,
        uid = uid,
        userName = authorName ?: "unknwon",
        type = type,
        level = level,
        tags = tags.orEmpty(),
        routeId = routeId,
        fWaypoints = waypoints.first,
        bWaypoints = waypoints.second,
        center = LatLngColumns(center.lat, center.lng),
        bounds = bounds,
        distanceM = LegInt(distance?.forward ?: 0, distance?.backward ?: 0),
        durationMils = LegLong(duration?.forward ?: 0, duration?.backward ?: 0),
        hide = hide ?: false,
        reportedCount = reportedCount ?: 0,
        updateAt = updateAt,
        createAt = createAt
    )
}

private fun dropped(reason: String, onDropped: (String) -> Unit): CourseEntity? {
    onDropped(reason)
    return null
}

private fun PointsDto.toLatLngColumns(): Pair<List<LatLngColumns>, List<LatLngColumns>>?{
    val fo= buildList {
        forward?.start?.toCols()?.let { add(it) }?:return null
        forward.via?.toCols()?.let { add(it) }
        forward.goal?.toCols() ?.let { add(it) }?:return null
    }
    val ba = buildList {
        backward?.start?.toCols()?.let { add(it) }?:return null
        backward.via?.toCols()?.let { add(it) }
        backward.goal?.toCols() ?.let { add(it) }?:return null
    }
    return fo to ba
}

private fun List<LatLngColumns>.toLatLngs(): List<LatLng>{
    return map{ LatLng(it.lat,it.lng) }
}

private fun LatLngDto.toCols(): LatLngColumns?{
    if(lat == null || lng == null) return null
    return LatLngColumns(lat, lng)
}

private fun LatLngDto?.toPoint(): GeoPoint? {
    val lat = this?.lat ?: return null
    val lng = this.lng ?: return null
    return GeoPoint(lat, lng)
}

private fun BoundsDto?.toBoundsColumns(): BoundsColumns? {
    val swLat = this?.swLat ?: return null
    val swLng = this.swLng ?: return null
    val neLat = this.neLat ?: return null
    val neLng = this.neLng ?: return null
    if (swLat > neLat || swLng > neLng) return null
    return BoundsColumns(swLat = swLat, swLng = swLng, neLat = neLat, neLng = neLng)
}

internal fun CourseEntity.toDomain(): Course = Course(
    id = id,
    title = title,
    uid = uid,
    userName = userName,
    type = type,
    level = level,
    tags = tags,
    routeId = routeId,
    fWaypoints = fWaypoints.toLatLngs(),
    bWaypoints = bWaypoints.toLatLngs(),
    center = LatLng(center.lat, center.lng),
    bounds = GeoBounds(bounds.swLat, bounds.swLng, bounds.neLat, bounds.neLng),
    distanceM = Leg(distanceM.forward, distanceM.backward),
    durationMils = Leg(durationMils.forward, durationMils.backward),
    hide = hide,
    reportedCount = reportedCount,
    forward = emptyList(),
    backward = emptyList(),
    updateAt = updateAt,
    createAt = createAt,
)