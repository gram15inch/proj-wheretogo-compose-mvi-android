package com.wheretogo.presentation.feature.naver

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.clustering.ClusteringKey
import com.naver.maps.map.clustering.MarkerManager
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class ClusterMarkerManager(
    val onReleaseMarker: (String) -> Unit
) : MarkerManager {
    val hideMarkerQue = ArrayDeque<Marker>()

    override fun retainMarker(markerInfo: com.naver.maps.map.clustering.MarkerInfo): Marker? {
        val marker = hideMarkerQue.removeFirstOrNull()
        return marker ?: Marker()
    }

    override fun releaseMarker(
        markerInfo: com.naver.maps.map.clustering.MarkerInfo,
        marker: Marker
    ) {
        val id = marker.tag
        if (id is String && id.isNotBlank()) {
            onReleaseMarker(marker.tag as String)
        }
        hideMarkerQue.addLast(marker)
    }
}

data class LeafItem(
    val leafId: String,
    val latLng:LatLng,
    val caption:String,
    val thumbnail:String,
    val overlayImage: OverlayImage,
    val onLeafClick: (String) -> Unit = {}
) : ClusteringKey {
    override fun getPosition(): LatLng {
        return latLng
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val otherLeafItem = other as LeafItem
        return leafId == otherLeafItem.leafId
    }

    override fun hashCode() = leafId.hashCode()

}

