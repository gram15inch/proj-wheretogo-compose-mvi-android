package com.dhkim139.feature.navigationoption

import androidx.annotation.StringRes
import com.dhkim139.feature.navigationoption.ui.NavPoint

data class NavOption(val courseId: String, val direction: CourseDirection)

data class FocusTarget(
    val role: EndpointRole?,
    val latLng: NavLatLng
)

data class NavOptionResult(
    val courseId: String,
    val courseName: String,
    val useMyLocation: Boolean,
    val points: List<NavPoint>,
    val mapApp: MapApp,
)

data class NavLatLng(
    val lat: Double,
    val lng: Double,
)

enum class EndpointRole(@StringRes val labelRes: Int) {
    START(R.string.course_start),
    END(R.string.course_end);

    fun opposite(): EndpointRole = if (this == START) END else START
}

enum class CourseDirection {
    FORWARD, BACKWARD
}

enum class MapApp(
    val id: String,
    @StringRes val labelRes: Int,
    val packageName: String,
    val supportsWaypoints: Boolean,
) {
    NAVER_MAP("naver_map", R.string.app_naver_map, "com.nhn.android.nmap", true),
    TMAP("tmap", R.string.app_tmap, "com.skt.tmap.ku", true),
    KAKAO_MAP("kakao_map", R.string.app_kakao_map, "net.daum.android.map", false),
    ;

    companion object {
        val DEFAULT: MapApp = NAVER_MAP
        fun fromIdOrDefault(id: String?): MapApp =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
