package com.wheretogo.domain


const val COURSE_TYPE = 0
const val CHECKPOINT_TYPE = 1
const val PATH_TYPE = 2

const val DAY = 86400000L
const val CHECKPOINT_UPDATE_TIME = DAY

const val USER_DATE_FORMAT = "yyyy-MM-dd"
const val DOMAIN_EMPTY = ""


enum class AuthCompany { GOOGLE }

enum class OverlayType {
    NONE, COURSE, CHECKPOINT, PATH
}

enum class HistoryType {
    COMMENT, COURSE, CHECKPOINT, LIKE, BOOKMARK, REPORT
}

enum class ReportType {
    USER, COURSE, COMMENT, CHECKPOINT
}

enum class RouteDetailType(val code: Int) {
    UNKNOWN(100), TAG(101), LEVEL(102), RECOMMEND(104)
}


enum class CourseDetail(val code: String, val type: RouteDetailType) {
    NONE("none", RouteDetailType.UNKNOWN),

    DRIVE("${RouteDetailType.TAG}-0001", RouteDetailType.TAG),
    SPORT("${RouteDetailType.TAG}-0002", RouteDetailType.TAG),
    TRAINING("${RouteDetailType.TAG}-0003", RouteDetailType.TAG),

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
    INVALID_USER, INVALID_DATA
}


class UserNotExistException(message: String) : NoSuchElementException(message)

