package com.wheretogo.data.model.route

import com.wheretogo.data.feature.PolylineCodec
import com.wheretogo.domain.model.course.RoutePath


fun RoutePathResponseDto.toEntity(routeId: String, now: Long): RoutePathEntity? {
    if (!isSupported) return null
    val f = forward ?: return null
    val b = backward ?: return null
    return RoutePathEntity(
        routeId = routeId,
        format = RoutePathResponseDto.FORMAT_POLYLINE5,
        forward = f,
        backward = b,
        bytes = f.length + b.length,
        lastUsedAt = now,
    )
}

fun RoutePathEntity.toDomain(): RoutePath? {
    val forwardPoints = PolylineCodec.decode(forward)
    val backwardPoints = PolylineCodec.decode(backward)

    if (forwardPoints.size < MIN_DRAWABLE_POINTS && backwardPoints.size < MIN_DRAWABLE_POINTS) {
        return null
    }
    return RoutePath(forward = forwardPoints, backward = backwardPoints)
}

private const val MIN_DRAWABLE_POINTS = 2
