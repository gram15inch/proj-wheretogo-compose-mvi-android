package com.wheretogo.data.model.course


data class CourseSyncResponseDto(
    val courses: List<CourseDto>? = null,
    val cursor: Long? = null, //  다음 문서 시간(더이상 가져올 문서가 없을경우 마지막 문서를 반환)
    val hasMore: Boolean? = null, // 가져온 문서 갯수(0일지라도)와 상관없이 뒤에 남은게 있을때 on
    val resync: Boolean? = null, // 서버 정책으로 마지막 커서가 오래됐다고 판단하면 on
)

data class CourseDto(
    val id: String? = null,
    val uid: String? = null,
    val authorName: String? = null,
    val title: String? = null,
    val type: String? = null,
    val level: String? = null,
    val tags: List<String>? = null,
    val routeId: String? = null,
    val points: PointsDto? = null,
    val center: LatLngDto? = null,
    val bounds: BoundsDto? = null,
    val distance: DistanceDto? = null,
    val duration: DurationDto? = null,
    val hide: Boolean? = null,
    val deleted: Boolean? = null,
    val reportedCount: Int? = null,
    val updateAt: Long? = null, // mils
    val createdAt: Long? = null,
)

data class PointsDto(
    val forward: DirectionPointsDto? = null,
    val backward: DirectionPointsDto? = null,
)

data class DirectionPointsDto(
    val start: LatLngDto? = null,
    val via: LatLngDto? = null,
    val goal: LatLngDto? = null,
)

data class LatLngDto(
    val lat: Double? = null,
    val lng: Double? = null,
)

data class BoundsDto(
    val swLat: Double? = null,
    val swLng: Double? = null,
    val neLat: Double? = null,
    val neLng: Double? = null,
)

data class DistanceDto(
    val forward: Int? = null,
    val backward: Int? = null,
)

data class DurationDto(
    val forward: Long? = null,
    val backward: Long? = null,
)

data class CourseEditRequestDto(
    val title: String? = null,
    val hide: Boolean? = null,
) {
    val isEmpty: Boolean get() = title == null && hide == null
}

data class Page(
    val upserts: List<CourseDto>,
    val deletedIds: List<String>,
    val cursor: Long?,
    val hasMore: Boolean,
    val resync: Boolean, // 서버가 판단한 전체 갱신여부
)