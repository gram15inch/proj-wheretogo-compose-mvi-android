package com.wheretogo.domain

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.RouteCategory

const val SECOND = 1000L
const val MIN = 60*SECOND
const val HOUR = 60*MIN
const val DAY = 24*HOUR
const val COURSE_UPDATE_TIME = DAY
const val ROUTE_GEOHASH_MIN_LENGTH = 4
const val USER_DATE_FORMAT = "yyyy-MM-dd"
const val DOMAIN_EMPTY = ""

const val LIST_ITEM_ZOOM = 12.0

sealed class DomainError : Exception() {
    data class NetworkError(val msg: String = "") : DomainError()
    data class UserInvalid(val msg: String = "") : DomainError()
    data class UnexpectedException(val throwable: Throwable) : DomainError()
}

fun <T> DomainError.toUseCaseResponse(): UseCaseResponse<T> {
    return when (this) {
        is DomainError.NetworkError -> {
            UseCaseResponse(
                UseCaseResponse.Status.Fail,
                failType = UseCaseFailType.NETWORK_ERROR
            )
        }

        is DomainError.UserInvalid -> {
            UseCaseResponse(
                UseCaseResponse.Status.Fail,
                failType = UseCaseFailType.GOOGLE_AUTH
            )
        }

        else -> {
            UseCaseResponse(
                UseCaseResponse.Status.Fail
            )
        }
    }
}

enum class AuthCompany { GOOGLE, PROFILE }

enum class HistoryType {
    COMMENT, COURSE, CHECKPOINT, LIKE, BOOKMARK, REPORT_CONTENT
}

enum class ReportType {
    USER, COURSE, COMMENT, CHECKPOINT
}

enum class SearchType {
    ADRESS, COURSE, ALL
}

enum class RouteAttr {
     TYPE, LEVEL, RELATION;

    fun getItems(): List<RouteCategory> {
       return when(this){
            TYPE-> typeCategoryGroup
            LEVEL-> levelCategoryGroup
            RELATION-> relationCategoryGroup
        }
    }
}

val typeCategoryGroup = listOf(
    RouteCategory(1001, RouteAttr.TYPE, RouteAttrItem.DRIVE),
    RouteCategory(1002, RouteAttr.TYPE, RouteAttrItem.SPORT),
    RouteCategory(1003, RouteAttr.TYPE, RouteAttrItem.TRAINING),
)

val levelCategoryGroup = listOf(
    RouteCategory(2001, RouteAttr.LEVEL, RouteAttrItem.BEGINNER),
    RouteCategory(2002, RouteAttr.LEVEL, RouteAttrItem.LOVER),
    RouteCategory(2003, RouteAttr.LEVEL, RouteAttrItem.EXPERT),
    RouteCategory(2004, RouteAttr.LEVEL, RouteAttrItem.PRO),
)

val relationCategoryGroup = listOf(
    RouteCategory(3001, RouteAttr.RELATION, RouteAttrItem.SOLO),
    RouteCategory(3002, RouteAttr.RELATION, RouteAttrItem.FRIEND),
    RouteCategory(3003, RouteAttr.RELATION, RouteAttrItem.FAMILY),
    RouteCategory(3004,  RouteAttr.RELATION,RouteAttrItem.COUPLE),
)

enum class RouteAttrItem{
    DRIVE,
    SPORT,
    TRAINING,

    BEGINNER,
    LOVER,
    EXPERT,
    PRO,

    SOLO,
    FRIEND,
    FAMILY,
    COUPLE;
}


enum class ReportStatus {
    PENDING, REVIEWED, REJECTED, ACCEPTED
}

enum class ImageSize(val pathName: String, val width: Int, val height: Int) {
    NORMAL("normal", 1500, 1500), SMALL("small", 200, 200)
}

enum class UseCaseFailType {
    INVALID_USER,
    INVALID_DATA,
    GOOGLE_AUTH,
    NETWORK_ERROR,
    USER_CREATE_ERROR,
}

enum class AuthType{
    TOKEN, PROFILE
}

class UserNotExistException(message: String) : NoSuchElementException(message)

fun zoomToGeohashLength(zoom: Double): Int {
    return when (zoom) {
        in 0.0..< 9.5 ->  3
        in 9.5..< 10.5 -> 4
        in 10.5..< 12.5 -> 4
        else -> 5

    }
}

fun Collection<Comment>.getFocusComment(): Comment {
    return this.maxWith(compareBy<Comment> { it.like }
        .thenByDescending { it.timestamp })
}
