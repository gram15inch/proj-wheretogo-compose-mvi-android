package com.wheretogo.presentation.feature.map

import com.wheretogo.domain.DomainError
import com.wheretogo.domain.FieldInvalidReason
import com.wheretogo.domain.RouteFieldType
import com.wheretogo.domain.feature.domainMap
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.naver.NaverMapOverlayModifier
import com.wheretogo.presentation.feature.naver.NaverMapOverlayStore
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import javax.inject.Inject

class CourseAddMapOverlayService @Inject constructor(
    private val overlayStore: NaverMapOverlayStore,
    private val overlayModifier: NaverMapOverlayModifier
) : MapOverlayService() {
    val overlays: Collection<MapOverlay> = _overlays.values

    private val WAYPOINT_PATH_ID = "WAYPOINT_PATH"

    fun addWaypoint(latlng: LatLng): Boolean {
        val markerGroup = _overlays.filter { it.value is MapOverlay.MarkerContainer }
        if (markerGroup.size < 5) {
            val id = "WAYPOINT_MARKER${System.currentTimeMillis()}"
            return overlayStore.getMarker(id) {
                overlayModifier.createMarker(
                    MarkerInfo(
                        id,
                        latlng,
                        iconRes = R.drawable.ic_mk_df
                    )
                )
            }.onSuccess {
                _overlays[id] = MapOverlay.MarkerContainer(
                    id, MarkerType.DEFAULT,
                    it
                )
            }.isSuccess
        }
        return false
    }

    fun removeWaypoint(id: String) {
        _overlays.remove(id)
        overlayStore.removeMarker(listOf(id))
        val markerGroup = _overlays.filter { it.value is MapOverlay.MarkerContainer }
        if (markerGroup.size < 2) {
            _overlays.remove(WAYPOINT_PATH_ID)
            overlayStore.removeMarker(listOf(WAYPOINT_PATH_ID))
        }
    }

    fun moveWaypoint(id: String, latlng: LatLng) {
        val old = _overlays[id]
        if (old != null && old is MapOverlay.MarkerContainer) {
            overlayStore.updateMarkerPosition(id, latlng)
            overlayStore.getMarker(
                id
            ) {
                overlayModifier.createMarker(
                    MarkerInfo(
                        contentId = id, position = latlng, iconRes = R.drawable.ic_mk_df
                    )
                )
            }.onSuccess {
                val overlay = MapOverlay.MarkerContainer(
                    id, MarkerType.DEFAULT, it
                )
                _overlays.replace(id, overlay)
            }
        }

    }

    fun hideWaypoint(id: String) {
        val marker = _overlays[id]
        if (marker != null && marker is MapOverlay.MarkerContainer) {
            marker.marker.replaceVisible(false)
        }
        _overlays.remove(WAYPOINT_PATH_ID)
        overlayStore.removePath(listOf(WAYPOINT_PATH_ID))

    }

    fun getWaypoints(): List<MapOverlay.MarkerContainer> {
        return _overlays.mapNotNull { if (it.value is MapOverlay.MarkerContainer) it.value as MapOverlay.MarkerContainer else null }
    }

    fun getWaypointPath(): MapOverlay.PathContainer? {
        val pathContainer = _overlays[WAYPOINT_PATH_ID]
        return pathContainer.run {
            this as? MapOverlay.PathContainer
        }
    }

    fun createScaffoldPath(): Result<Unit> {
        return runCatching {
            val waypoints =
                _overlays.mapNotNull { (it.value as? MapOverlay.MarkerContainer)?.marker?.markerInfo?.position }

            if (waypoints.isEmpty()) {
                return Result.failure(AppError.Ignore())
            }
            val id = WAYPOINT_PATH_ID
            PathInfo(
                contentId = id,
                points = waypoints,
                minZoomLevel = 0.0,
            )
        }.domainMap { pathInfo ->
            overlayStore.updatePath(pathInfo).run {
                if (this.exceptionOrNull() is DomainError.NotFound)
                    overlayStore.getPath(
                        contentId = pathInfo.contentId,
                        initPath = { overlayModifier.createPath(pathInfo) }
                    )
                else
                    this
            }.onSuccess {
                val pathContainer =
                    MapOverlay.PathContainer(pathInfo.contentId, PathType.SCAFFOLD, it)
                _overlays[pathInfo.contentId] = pathContainer
            }
        }.map{}
    }

    fun createFullPath(points: List<LatLng> = emptyList()): Result<Unit> {
        return runCatching {
            if (points.size < 2) {
                return Result.failure(
                    DomainError.RouteFieldInvalid(
                        RouteFieldType.POINT,
                        FieldInvalidReason.MIN
                    )
                )
            }

            val id = WAYPOINT_PATH_ID
            val pathInfo = PathInfo(
                contentId = id,
                points = points,
                minZoomLevel = 0.0,
            )
            pathInfo
        }.domainMap { pathInfo ->
            overlayStore.updatePath(pathInfo).run {
                if (this.exceptionOrNull() is DomainError.NotFound)
                    overlayStore.getPath(
                        contentId = pathInfo.contentId,
                        initPath = { overlayModifier.createPath(pathInfo) }
                    )
                else
                    this
            }.onSuccess {
                val pathContainer =
                    MapOverlay.PathContainer(pathInfo.contentId, PathType.FULL, it)
                _overlays[pathInfo.contentId] = pathContainer
            }
        }.map { }
    }
}