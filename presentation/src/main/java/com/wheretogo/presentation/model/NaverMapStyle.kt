package com.wheretogo.presentation.model

import com.naver.maps.map.NaverMap
import com.wheretogo.domain.ZOOM

data class NaverMapStyle(
    // 지도 종류
    val mapType: NaverMap.MapType = NaverMap.MapType.Basic,

    // 줌
    val maxZoom: Double = ZOOM.Place.level,
    val minZoom: Double = ZOOM.COUNTRY.level,

    // 레이어 그룹 on/off
    val buildingEnabled: Boolean = false,
    val transitEnabled: Boolean = false,
    val bicycleEnabled: Boolean = false,
    val trafficEnabled: Boolean = false,
    val cadastralEnabled: Boolean = false,
    val mountainEnabled: Boolean = false,

    // POI 심볼
    val symbolScale: Float = 0f,
    val symbolPerspectiveRatio: Float = 0f,

    // 모드
    val indoorEnabled: Boolean = false,
    val nightModeEnabled: Boolean = false,

    // 제스처
    val scrollGesturesEnabled: Boolean = false,
    val zoomGesturesEnabled: Boolean = false,
    val tiltGesturesEnabled: Boolean = false,
    val rotateGesturesEnabled: Boolean = false,

    // UI 컨트롤
    val zoomControlEnabled: Boolean = false,
    val compassEnabled: Boolean = false,
    val scaleBarEnabled: Boolean = false,
    val locationButtonEnabled: Boolean = false,
    val logoClickEnabled: Boolean = false,

) {
    companion object {
        val Basic = NaverMapStyle(
            symbolScale = 1f,
            symbolPerspectiveRatio = 1f,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            tiltGesturesEnabled = true,
            rotateGesturesEnabled = true,
            compassEnabled = true,
            scaleBarEnabled = true,
            locationButtonEnabled = true
        )

        val Place = NaverMapStyle()
    }
}


