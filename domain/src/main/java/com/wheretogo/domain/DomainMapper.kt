package com.wheretogo.domain


import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.auth.SyncProfile
import com.wheretogo.domain.model.auth.SyncToken
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.model.checkpoint.CheckPointContent
import com.wheretogo.domain.model.comment.CommentAddRequest
import com.wheretogo.domain.model.comment.CommentContent
import com.wheretogo.domain.model.course.CourseAddRequest
import com.wheretogo.domain.model.course.CourseContent
import com.wheretogo.domain.model.history.HistoryIdGroup
import com.wheretogo.domain.model.report.ReportAddRequest
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.usecase.report.ReportContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun CourseContent.toCourseAddRequest(
    keyword: List<String>,
    userId:String,
    userName:String
): CourseAddRequest {
    return CourseAddRequest(
        content = this,
        keyword = keyword,
        userId = userId,
        userName = userName
    )
}

fun CheckPointContent.toAddRequest(
    imageId: String,
    groupId: String? = null, // 선택된 코스에서 실제 Id 삽입시 사용
): CheckPointAddRequest {
    val content = this.run {
        if (groupId == null)
            this
        else
            this.copy(groupId = groupId)
    }
    return CheckPointAddRequest(
        content = content,
        imageId = imageId,
    )
}

fun CommentContent.toAddRequest(
    userId:String,
    userName:String,
    groupId: String? = null,
): CommentAddRequest {
    return CommentAddRequest(
        content = this,
        groupId = groupId ?: this.checkPointId,
        userId = userId,
        userName = userName
    )
}

fun ReportContent.toAddRequest(
    reporterId: String,
    contentGroupId: String? = null
): ReportAddRequest {
    return ReportAddRequest(
        contentId = contentId,
        contentGroupId = contentGroupId ?: this.contentGroupId,
        type = type,
        reason = reason,
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        reporterId = reporterId
    )
}

fun History.map(type: HistoryType, data: Map<String, HashSet<String>>): History {
    return when (type) {
        HistoryType.COURSE -> {
            copy(course = course.copy(type, HistoryIdGroup(data)))
        }

        HistoryType.CHECKPOINT -> {
            copy(checkpoint = checkpoint.copy(type, HistoryIdGroup(data)))
        }

        HistoryType.COMMENT -> {
            copy(comment = comment.copy(type, HistoryIdGroup(data)))
        }

        HistoryType.REPORT -> {
            copy(checkpoint = checkpoint.copy(type, HistoryIdGroup(data)))
        }

        HistoryType.LIKE -> {
            copy(like = like.copy(type, HistoryIdGroup(data)))
        }
    }
}

fun History.get(type: HistoryType): HistoryIdGroup {
    return when (type) {
        HistoryType.COURSE -> {
            course.historyIdGroup
        }

        HistoryType.CHECKPOINT -> {
            checkpoint.historyIdGroup
        }

        HistoryType.COMMENT -> {
            comment.historyIdGroup
        }

        HistoryType.REPORT -> {
            report.historyIdGroup
        }

        HistoryType.LIKE -> {
            like.historyIdGroup
        }
    }
}

fun LatLng.toGeoHash(length: Int): String {
    return GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude), length)
}

fun LatLng.toGeoHashBound(radius: Double): Pair<String, String> {
    return GeoFireUtils.getGeoHashQueryBounds(GeoLocation(latitude, longitude), radius).first()
        .run { Pair(startHash, endHash) }
}


fun formatMillisToDate(millis: Long, pattern: String = USER_DATE_FORMAT): String {
    val date = Date(millis)
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(date)
}

fun parseDateToMillis(dateString: String, pattern: String = USER_DATE_FORMAT): Long {
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    val date = dateFormat.parse(dateString)
    return date?.time ?: throw IllegalArgumentException("Invalid date format or value")
}

fun SyncProfile.toSyncToken(): SyncToken {
    return SyncToken(
        authCompany = authCompany,
        idToken = idToken,
        msgToken = msgToken
    )
}