package com.wheretogo.presentation.feature.naver

import com.wheretogo.domain.DomainError
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.AppPath
import com.wheretogo.presentation.model.PathInfo
import javax.inject.Inject


class NaverMapOverlayStore @Inject constructor() {
    private val markers = mutableMapOf<String, AppMarker>() // 컨텐츠 id
    private val paths = mutableMapOf<String, AppPath>() // 컨텐츠 id

    fun updateMarkerVisible(contentId: String, isVisible: Boolean) {
        markers.get(contentId)?.let {
            val newMarker = it.replaceVisible(isVisible)
            markers.replace(contentId, newMarker)
        }
    }

    fun updateMarkerCaption(contentId: String, caption: String) {
        markers.get(contentId)?.let {
            val newMarker = it.replaceCation(caption)
            markers.replace(contentId, newMarker)
        }
    }

    fun updateMarkerPosition(contentId: String, latLng: LatLng) {
        markers.get(contentId)?.let {
            val newMarker = it.replacePosition(latLng)
            markers.replace(contentId, newMarker)
        }
    }

    fun updatePathVisible(contentId: String, isVisible: Boolean) {
        paths.get(contentId)?.let {
            val newPath = it.replaceVisible(isVisible)
            paths.replace(contentId, newPath)
        }
    }

    fun getMarker(contentId: String, initMarker: () -> AppMarker): AppMarker {
        return markers.getOrPut(contentId) {
            initMarker()
        }
    }

    fun getPath(contentId: String, initPath: () -> AppPath): Result<AppPath> {
        return runCatching {
            paths.getOrPut(contentId) {
                initPath()
            }
        }
    }

    fun updatePath(pathInfo: PathInfo): Result<AppPath> {
        return runCatching {
            if (pathInfo.points.size < 2)
                return Result.failure(DomainError.InternalError("points.size < 2"))

            val oldPath = paths.get(pathInfo.contentId)

            if (oldPath == null)
                return Result.failure(DomainError.NotFound("path empty"))
            oldPath.replacePoints(pathInfo.points)
        }
    }

    fun remove(id: String) {
        markers[id]?.reflectClear()
        markers.remove(id)
        paths[id]?.reflectClear()
        paths.remove(id)
    }

    fun size() = markers.size + paths.size

    fun clear(){
        markers.forEach {
            it.value.coreMarker?.apply { map=null }
        }
        markers.clear()
        paths.forEach {
            it.value.corePathOverlay?.apply { map=null }
        }
        paths.clear()
    }
}