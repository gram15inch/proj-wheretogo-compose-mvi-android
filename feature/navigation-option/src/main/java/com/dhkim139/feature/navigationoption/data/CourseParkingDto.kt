package com.dhkim139.feature.navigationoption.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CourseParkingOneWayDto(
    val courseId: String,
    val route: CourseRoute = CourseRoute.FORWARD,
    val status: CourseParkingStatus = CourseParkingStatus.READY,
    val computedAt: Long = 0L,
    val start: CourseParkingGroup = CourseParkingGroup(),
    val goal: CourseParkingGroup = CourseParkingGroup(),
)

@JsonClass(generateAdapter = true)
data class CourseParkingGroup(
    val role: ParkingGroupRole = ParkingGroupRole.START,
    val point: LatLng? = null,
    val lots: List<ParkingOneWayEntry> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ParkingOneWayEntry(
    val lotId: String,
    val name: String,
    val location: LatLng,
    val isOfficial: Boolean = true,     // 공공 데이터
    val category: ParkingCategory = ParkingCategory.ETC,
    val usage: ParkingUsage = ParkingUsage.PARKING,
    val tags: List<ParkingTag> = emptyList(),
    val capacityLevel: CapacityLevel = CapacityLevel.UNKNOWN,
    val travelSec: Int = 0,             // 경로 시간
    val travelDistanceM: Int = 0,
    val straightDistanceM: Int = 0,     // 직선거리
    val isEstimated: Boolean = false,   // 추정치
    val rank: Int = 0,
    val estimatedFee: Int? = null,      // 참고용 요금
    val feeNote: String? = null,        // 등록자가 작성한 요금 설명 ex) 영화관람시 3시간 무료
    val feeBaseDate: String? = null,    // 요금 기준 일자
    val cautionNote: String? = null,    // 특이사항
    val adminNote: String? = null,
    val thumbnailUrl: String? = null,
) {
    val knownTags: List<ParkingTag>
        get() = tags.filter { it != ParkingTag.UNKNOWN }
}

enum class ParkingGroupRole {
    START,
    GOAL,
}

@JsonClass(generateAdapter = true)
data class CourseParkingView(
    val courseId: String,
    val route: CourseRoute = CourseRoute.FORWARD,
    val status: CourseParkingStatus = CourseParkingStatus.READY,
    val computedAt: Long = 0L,
    val entryPoint: LatLng? = null,
    val exitPoint: LatLng? = null,
    val lots: List<ParkingViewEntry> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ParkingViewEntry(
    val lotId: String,
    val name: String,
    val location: LatLng,
    val isOfficial: Boolean = true,
    val category: ParkingCategory = ParkingCategory.ETC,
    val usage: ParkingUsage = ParkingUsage.PARKING,
    val tags: List<ParkingTag> = emptyList(),
    val capacityLevel: CapacityLevel = CapacityLevel.UNKNOWN,
    val inSec: Int = 0,
    val outSec: Int? = null,
    val totalSec: Int? = null,
    val isEstimated: Boolean = false,
    val rank: Int = 0,
    val inDistanceM: Int = 0,
    val outDistanceM: Int? = null,
    val straightDistanceM: Int = 0,
    val estimatedFee: Int? = null,
    val feeNote: String? = null,
    val feeBaseDate: String? = null,
    val cautionNote: String? = null,
    val adminNote: String? = null,
    val thumbnailUrl: String? = null,
) {
    val knownTags: List<ParkingTag>
        get() = tags.filter { it != ParkingTag.UNKNOWN }

    val outInRatio: Float?
        get() {
            val out = outSec ?: return null
            if (inSec <= 0) return null
            return out.toFloat() / inSec.toFloat()
        }
}

@JsonClass(generateAdapter = true)
data class LatLng(
    val lat: Double,
    val lng: Double,
)

enum class CourseRoute {
    FORWARD,
    BACKWARD,
}

enum class CourseParkingStatus {
    PENDING,
    READY,
    PARTIAL,
    FAILED,
}

enum class ParkingCategory {
    ON_STREET,
    OFF_STREET,
    ATTACHED,
    VACANT_LOT,
    ROADSIDE,
    REST_STOP,
    PRIVATE_YARD,
    ETC,
}

enum class ParkingUsage {
    PARKING,
    STOPOVER,
    BOTH,
}

enum class CapacityLevel {
    FEW, // 1~3대
    SOME, // 4~10대
    MANY, // 10대 이상
    UNKNOWN,
}

enum class ParkingTag {
    FREE,
    NIGHT_OK,
    RESTROOM,
    SCENIC,
    EV_CHARGER,
    LARGE_VEHICLE,
    CROWDED,
    SEASONAL,
    NEEDS_PERMISSION,
    ACCESSIBLE,
    UNKNOWN,
}