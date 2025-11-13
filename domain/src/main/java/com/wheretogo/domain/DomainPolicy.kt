package com.wheretogo.domain

import com.wheretogo.domain.model.route.RouteCategory
import java.util.concurrent.TimeUnit

const val SECOND = 1000L
const val MIN = 60 * SECOND
const val HOUR = 60 * MIN
const val DAY = 24 * HOUR
const val COURSE_UPDATE_TIME = DAY
const val USER_DATE_FORMAT = "yyyy-MM-dd"
const val LOG_DATE_FORMAT = "yyyy-MM-dd H:m:s"
const val DOMAIN_EMPTY = ""

const val ROUTE_MIN_ZOOM = 9.5
const val CHECKPOINT_MIN_ZOOM = 9.5
const val LIST_ITEM_ZOOM = 12.0

val CourseCooldown = DefaultCoolDownPolicy(600)
val CheckpointCooldown = DefaultCoolDownPolicy(15)
val CommentCooldown = DefaultCoolDownPolicy(1)

sealed class DomainError : Exception() {
    data class NetworkError(val msg: String = "") : DomainError()
    data class UserInvalid(val msg: String = "") : DomainError()
    data class Unauthorized(val msg: String = "") : DomainError()
    data class UserUnavailable(val msg: String = "") : DomainError()
    data class SignInError(val msg: String = "") : DomainError()
    data class RouteFieldInvalid(
        val type: RouteFieldType,
        val reason: FieldInvalidReason = FieldInvalidReason.NONE
    ) : DomainError()
    data class NotFound(val msg: String = "") : DomainError()
    data class InternalError(val msg: String = "") : DomainError()
    data class ExpireData(val msg: String = "") : DomainError()
    data class CoolDownData(val remainingMinutes: Int = 0) : DomainError()
    data class UnexpectedException(val throwable: Throwable) : DomainError()
}

enum class RouteFieldType { NAME, POINT, KEYWORD }

enum class FieldInvalidReason { NONE, MAX, MIN, FORMAT }

fun Throwable?.toDomainError(): DomainError {
    return when (this) {
        is DomainError -> this
        is IllegalArgumentException -> DomainError.InternalError(this.message ?: "조건 오류")
        is IllegalStateException -> DomainError.InternalError(this.message ?: "상태 오류")
        null -> DomainError.InternalError("알수없는 오류")
        else -> DomainError.UnexpectedException(this)
    }
}

enum class TutorialStep {
    SKIP, HOME_TO_DRIVE, DRIVE_LIST_ITEM_CLICK;
    fun next(): TutorialStep{
        return TutorialStep.entries.getOrNull(ordinal+1)?:SKIP
    }
}

enum class AuthCompany { GOOGLE, PROFILE }

enum class HistoryType {
    COMMENT, COURSE, CHECKPOINT, LIKE, REPORT
}

enum class ReportType {
    USER, COURSE, COMMENT, CHECKPOINT
}

enum class SearchType {
    ADDRESS, COURSE, ALL
}

enum class AuthType {
    TOKEN, PROFILE
}

enum class RouteAttr {
    TYPE, LEVEL, RELATION;

    fun getItems(): List<RouteCategory> {
        return when (this) {
            TYPE -> typeCategoryGroup
            LEVEL -> levelCategoryGroup
            RELATION -> relationCategoryGroup
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
    RouteCategory(3004, RouteAttr.RELATION, RouteAttrItem.COUPLE),
)

enum class RouteAttrItem {
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
    NORMAL("normal", 1500, 1500),
    SMALL("small", 400, 400)
}

fun zoomToGeohashLength(zoom: Double): Int {
    return when (zoom) {
        in 0.0..<9.5 -> 3
        in 9.5..<10.5 -> 4
        in 10.5..<12.5 -> 4
        else -> 4

    }
}

sealed interface CoolDownPolicy {
    fun isCoolDown(timestamp: Long): Result<Unit>
}

class DefaultCoolDownPolicy(
   private val minCoolDownMinutes:Int
) : CoolDownPolicy {
    override fun isCoolDown(timestamp: Long): Result<Unit> {
        val waitingMinutes=
            TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamp).toInt()
        return if (minCoolDownMinutes > waitingMinutes)
            Result.failure(DomainError.CoolDownData(minCoolDownMinutes - waitingMinutes))
        else Result.success(Unit)
    }
}