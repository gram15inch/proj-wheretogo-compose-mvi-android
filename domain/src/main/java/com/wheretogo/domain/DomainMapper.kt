package com.wheretogo.domain


import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.model.checkpoint.CheckPointContent
import com.wheretogo.domain.model.comment.CommentAddRequest
import com.wheretogo.domain.model.comment.CommentContent
import com.wheretogo.domain.model.course.CourseAddRequest
import com.wheretogo.domain.model.course.CourseContent
import com.wheretogo.domain.model.history.HistoryIdGroup
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.util.Image
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun CourseContent.toCourseAddRequest(
    profile: Profile,
    keyword: List<String>
): CourseAddRequest {
    return CourseAddRequest(
        content = this,
        profile = profile,
        keyword = keyword
    )
}

fun CheckPointContent.toCheckPointAddRequest(
    profile: Profile,
    image: Image
): CheckPointAddRequest {
    return CheckPointAddRequest(
        content = this,
        image = image,
        profile = profile
    )
}

fun CommentContent.toCommentAddRequest(
    profile: Profile
): CommentAddRequest {
    return CommentAddRequest(
        content = this,
        profile = profile
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

fun AuthProfile.toProfile(hashMail:String): Profile {
    return Profile(
        uid = uid,
        name = userName,
        hashMail = hashMail,
        private = ProfilePrivate(
            mail = email,
            authCompany = authCompany.name,
            accountCreation = System.currentTimeMillis()
        )
    )
}