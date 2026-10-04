package com.wheretogo.domain.model.course

import com.wheretogo.domain.model.address.LatLng

data class GeoPoint(val lat: Double, val lng: Double)

data class GeoBounds(
    val swLat: Double,
    val swLng: Double,
    val neLat: Double,
    val neLng: Double,
)

data class Leg<T>(val forward: T, val backward: T)

data class Course(
    val id: String,
    val title: String,
    val uid: String,
    val userName: String,
    val type: String,
    val level: String,
    val tags: List<String>,
    val routeId: String,
    val fWaypoints: List<LatLng>,
    val bWaypoints: List<LatLng>,
    val forward: List<LatLng>,
    val backward: List<LatLng>,
    val center: LatLng,
    val bounds: GeoBounds,
    val distanceM: Leg<Int>,
    val durationMils: Leg<Long>,
    val reportedCount: Int,
    val hide: Boolean = false,
    val delete: Boolean = false,
    val mine: Boolean = false,
    val updateAt: Long,
    val createAt: Long,
) {
    companion object {
        val dummy =
            Course(
                id = "",
                title = "",
                uid = "",
                userName = "",
                type = "",
                level = "",
                tags = emptyList(),
                routeId = "",
                fWaypoints = emptyList(),
                bWaypoints = emptyList(),
                forward = emptyList(),
                backward = emptyList(),
                center = LatLng(0.0, 0.0),
                bounds = GeoBounds(0.0, 0.0, 0.0, 0.0),
                distanceM = Leg(0, 0),
                durationMils = Leg(0, 0),
                reportedCount = 0,
                hide = false,
                mine = false,
                updateAt = 0L,
                createAt = 0L
            )
    }

    fun toDirectionItem(direction: StartDirection = StartDirection.FORWARD): CourseRenderItem {
        return CourseRenderItem(
            courseId = id,
            uid = uid,
            userName = userName,
            fWaypoint = fWaypoints,
            bWaypoint = bWaypoints,
            center = center,
            title = title,
            type = type,
            level = level,
            duration = durationMils,
            isUserCreate = mine,
            tags = tags,
            direction = direction
        )
    }
}


fun Course.isVisible(reported: Set<String>): Boolean = !hide && !delete && id !in reported

data class RoutePath(
    val forward: List<GeoPoint>,
    val backward: List<GeoPoint>,
)

data class SyncState(
    val cursor: Long, // 서버가 준 마지막 갱신 일자. 자체 삽입 x
    val lastSyncedAt: Long?, // 기기에 저장하는 최근 갱신 일자
) {
    companion object {
        val EMPTY = SyncState(cursor = 0, lastSyncedAt = null)
    }
}

data class MapCamera(
    val zoom: Double,
    val target: GeoPoint,
    val viewport: GeoBounds,
)

data class BoundingBox(
    val southWest: LatLng,
    val northEast: LatLng,
) {
    companion object {
        fun of(points: List<LatLng>): BoundingBox? {
            if (points.isEmpty()) return null
            return BoundingBox(
                southWest = LatLng(points.minOf { it.latitude }, points.minOf { it.longitude }),
                northEast = LatLng(points.maxOf { it.latitude }, points.maxOf { it.longitude }),
            )
        }
    }
}

data class CameraFocus(
    val target: LatLng? = null,
    val bounds: BoundingBox? = null,
    val bottomPaddingPx: Int = 0,
    val zoom: Double? = null,
)

fun List<GeoPoint>?.toLatlng() =
    this?.let { it.map { LatLng(it.lat, it.lng) } } ?: emptyList()