package com.wheretogo.presentation.feature.naver

import android.util.Log
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


fun getJourneyOverlay(item: Journey, onMarkerClick: (Overlay) -> Unit):JourneyOverlay{
    val naverPoints = item.points.toNaver()
    return JourneyOverlay(
        item.code,
        Marker().apply {
            position = naverPoints[0]
            tag = item.code
            this.setOnClickListener { overlay ->
                onMarkerClick(overlay)
                true
            }
        },
        PathOverlay().apply {
            coords = naverPoints
            tag= item.code
        }
    )
}