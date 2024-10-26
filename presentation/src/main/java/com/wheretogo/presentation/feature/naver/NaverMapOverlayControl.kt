package com.wheretogo.presentation.feature.naver

import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import com.wheretogo.domain.model.Journey
import com.wheretogo.presentation.model.JourneyOverlay
import com.wheretogo.presentation.model.toNaver

fun NaverMap.addJourneyOverlay(item: Journey, onMarkerClick: (Overlay) -> Unit):JourneyOverlay{
    val naverPoints = item.points.toNaver()
    return JourneyOverlay(
        item.code,
        Marker().apply {
            position = naverPoints[0] // todo 인덱스 에러 처리
            map = this@addJourneyOverlay
            tag = item.code
            this.setOnClickListener { overlay ->
                onMarkerClick(overlay)
                true
            }
        },
        PathOverlay().apply {
            coords = naverPoints
            map = this@addJourneyOverlay
            tag= item.code
        }
    )
}


fun getJourneyOverlay(item: Journey):JourneyOverlay{
    val naverPoints = item.points.toNaver()
    return JourneyOverlay(
        item.code,
        Marker().apply {
            this.captionText = item.code.toString()
            position = naverPoints[0]
            tag = item.code
        },
        PathOverlay().apply {
            coords = naverPoints
            tag= item.code
            width = 18
            outlineWidth = 3
        }
    )
}


class HideOverlayMap(
    var naverMap: NaverMap?,
    private val innerMap: MutableMap<Int,JourneyOverlay> = mutableMapOf()
) : MutableMap<Int, JourneyOverlay>by innerMap {

    override fun put(key: Int, value: JourneyOverlay): JourneyOverlay? {
        hide(value)
        return innerMap.put(key, value)
    }

    override fun remove(key: Int): JourneyOverlay? {
        innerMap[key]?.let {
            show(it)
        }
        return innerMap.remove(key)
    }

    override fun clear() {
        innerMap.forEach {
            show(it.value)
        }
        innerMap.clear()
    }

    private fun show(element: JourneyOverlay) {
        element.marker.isVisible = true
        element.pathOverlay.isVisible = true
        naverMap?.apply {
            element.marker.map = this
            element.pathOverlay.map = this
        }
    }

    private fun hide(element: JourneyOverlay) {
        element.marker.isVisible = false
        element.pathOverlay.isVisible = false
        naverMap?.let {
            element.marker.map = it
            element.pathOverlay.map = it
        }
    }
}

