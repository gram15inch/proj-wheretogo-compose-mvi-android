package com.wheretogo.domain


const val COURSE_TYPE = 0
const val CHECKPOINT_TYPE = 1
const val PATH_TYPE = 2

const val SECOND = 1000L
const val MIN = 60*SECOND
const val HOUR = 60*MIN
const val DAY = 24*HOUR
const val CHECKPOINT_UPDATE_TIME = 3*HOUR
const val COURSE_UPDATE_TIME = DAY
const val ROUTE_GEOHASH_MIN_LENGTH = 4
const val USER_DATE_FORMAT = "yyyy-MM-dd"
const val DOMAIN_EMPTY = ""


enum class AuthCompany { GOOGLE, PROFILE }

enum class OverlayType {
    NONE, COURSE, CHECKPOINT, PATH
}

enum class HistoryType {
    COMMENT, COURSE, CHECKPOINT, LIKE, BOOKMARK, REPORT_CONTENT
}

enum class ReportType {
    USER, COURSE, COMMENT, CHECKPOINT
}

enum class RouteDetailType(val code: Int) {
    UNKNOWN(100), TYPE(101), LEVEL(102), RECOMMEND(104)
}

enum class CourseDetail(val code: String, val type: RouteDetailType) {
    NONE("none", RouteDetailType.UNKNOWN),

    DRIVE("${RouteDetailType.TYPE}-0001", RouteDetailType.TYPE),
    SPORT("${RouteDetailType.TYPE}-0002", RouteDetailType.TYPE),
    TRAINING("${RouteDetailType.TYPE}-0003", RouteDetailType.TYPE),

    BEGINNER("${RouteDetailType.LEVEL}-0001", RouteDetailType.LEVEL),
    LOVER("${RouteDetailType.LEVEL}-0002", RouteDetailType.LEVEL),
    EXPERT("${RouteDetailType.LEVEL}-0003", RouteDetailType.LEVEL),
    PRO("${RouteDetailType.LEVEL}-0004", RouteDetailType.LEVEL),

    SOLO("${RouteDetailType.RECOMMEND}-0001", RouteDetailType.RECOMMEND),
    FRIEND("${RouteDetailType.RECOMMEND}-0002", RouteDetailType.RECOMMEND),
    FAMILY("${RouteDetailType.RECOMMEND}-0003", RouteDetailType.RECOMMEND),
    COUPLE("${RouteDetailType.RECOMMEND}-0004", RouteDetailType.RECOMMEND);

    companion object {
        fun fromCode(code: String): CourseDetail {
            return entries.find { it.code == code } ?: NONE
        }
    }
}


enum class ReportStatus {
    PENDING, REVIEWED, REJECTED, ACCEPTED
}

enum class ImageSize(val pathName: String, val width: Int, val height: Int) {
    NORMAL("normal", 1500, 1500), SMALL("small", 200, 200)
}

enum class UseCaseFailType {
    INVALID_USER, INVALID_DATA,
}

enum class AuthType{
    TOKEN, PROFILE
}

fun zoomToGeohashLength(zoom: Double): Int {
    return when (zoom) {
        in 0.0..< 9.5 ->  3
        in 9.5..< 10.5 -> 4
        in 10.5..< 12.5 -> 4
        else -> 5

    }
}

class UserNotExistException(message: String) : NoSuchElementException(message)