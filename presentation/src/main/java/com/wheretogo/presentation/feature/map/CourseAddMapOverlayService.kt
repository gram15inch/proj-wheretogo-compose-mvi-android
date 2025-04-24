package com.wheretogo.presentation.feature.map

import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.presentation.MarkerType
import com.wheretogo.presentation.PathType
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.naver.NaverMapOverlayStore
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.PathInfo
import com.wheretogo.presentation.toDomain
import com.wheretogo.presentation.toNaver
import java.util.UUID
import javax.inject.Inject

class CourseAddMapOverlayService @Inject constructor(private val overlayStore: NaverMapOverlayStore) :
    MapOverlayService() {
    private val WAYPOINT_PATH_ID = "WAYPOINT_PATH"

    fun addWaypoint(latlng: LatLng): Boolean {
        val markerGroup = _overlays.filter { it.value is MapOverlay.MarkerContainer }
        if (markerGroup.size < 5) {
            val id = "WAYPOINT_MARKER${UUID.randomUUID()}"
            val marker = MapOverlay.MarkerContainer(
                id, MarkerType.CHECKPOINT,
                overlayStore.getOrCreateMarker(
                    MarkerInfo(
                        id,
                        latlng,
                        iconRes = R.drawable.ic_mk_df
                    )
                )
            )
            _overlays[id] = marker
            return true
        }
        return false
    }

    fun removeWaypoint(id: String) {
        _overlays.remove(id)
        overlayStore.remove(id)
        val markerGroup = _overlays.filter { it.value is MapOverlay.MarkerContainer }
        if (markerGroup.size < 2) {
            _overlays.remove(WAYPOINT_PATH_ID)
            overlayStore.remove(WAYPOINT_PATH_ID)
        }
    }

    fun moveWaypoint(id: String, latlng: LatLng) {
        val old = _overlays[id]
        if (old != null && old is MapOverlay.MarkerContainer) {
            old.marker.position = latlng.toNaver()
            old.marker.isVisible = true
        }
    }

    fun hideWaypoint(id: String) {
        val marker = _overlays[id]
        if (marker != null && marker is MapOverlay.MarkerContainer) {
            marker.marker.isVisible = false
        }
        _overlays.remove(WAYPOINT_PATH_ID)
        overlayStore.remove(WAYPOINT_PATH_ID)

    }

    fun getWaypoints(): List<MapOverlay.MarkerContainer> {
        return _overlays.mapNotNull { if (it.value is MapOverlay.MarkerContainer) it.value as MapOverlay.MarkerContainer else null }
    }

    fun getWaypointPath(): MapOverlay.PathContainer? {
        val pathContainer = _overlays[WAYPOINT_PATH_ID]
        return pathContainer.run {
            if (this is MapOverlay.PathContainer)
                this
            else null
        }
    }

    fun createWaypointPath(points: List<LatLng> = emptyList()): Boolean {
        val waypoints = _overlays.mapNotNull {
            if (it.value is MapOverlay.MarkerContainer) {
                (it.value as MapOverlay.MarkerContainer).marker.position
            } else {
                null
            }
        }.toDomain()
        if (waypoints.size >= 2) {
            val id = WAYPOINT_PATH_ID
            val pathType = if (points.isNotEmpty()) PathType.FULL else PathType.PARTIAL
            val result = when (pathType) {
                PathType.FULL -> points
                PathType.PARTIAL -> waypoints
            }
            overlayStore.getOrCreatePath(
                PathInfo(
                    contentId = id,
                    points = result,
                    minZoomLevel = 0.0,
                )
            )?.let {
                val pathContainer = MapOverlay.PathContainer(id, pathType, it)
                _overlays[id] = pathContainer
            }
            return true
        }

        return false
    }
}