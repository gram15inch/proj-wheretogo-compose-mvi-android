package com.wheretogo.domain


import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.CheckPointAddRequest
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.CourseAddRequest
import com.wheretogo.domain.model.map.History
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.model.user.AuthProfile
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Course.toCourseAddRequest(): CourseAddRequest {
    return CourseAddRequest(
        courseName = courseName,
        waypoints = waypoints,
        points = points,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
    )
}

fun CourseAddRequest.toCourse(
    courseId: String,
    userId: String,
    userName: String
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        waypoints = waypoints,
        points = points,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
    )
}


fun Profile.toAuthProfile(token:String): AuthProfile {
    return AuthProfile(
        uid = uid,
        email = private.mail,
        userName = name,
        authCompany = AuthCompany.PROFILE,
        token = token
    )
}

fun List<Pair<HistoryType, HashSet<String>>>.toHistory(): History {
    var history = History()
    this.forEach {
        history = history.map(it.first, it.second)
    }
    return history
}

fun History.map(type: HistoryType, data: HashSet<String>): History {
    return when (type) {
        HistoryType.COURSE -> {
            copy(courseGroup = data)
        }

        HistoryType.CHECKPOINT -> {
            copy(checkpointGroup = data)
        }

        HistoryType.COMMENT -> {
            copy(commentGroup = data)
        }

        HistoryType.REPORT_CONTENT -> {
            copy(reportGroup = data)
        }

        HistoryType.LIKE -> {
            copy(likeGroup = data)
        }

        HistoryType.BOOKMARK -> {
            copy(bookmarkGroup = data)
        }
    }
}

fun History.get(type: HistoryType): HashSet<String> {
    return when (type) {
        HistoryType.COURSE -> {
            courseGroup
        }

        HistoryType.CHECKPOINT -> {
            checkpointGroup
        }

        HistoryType.COMMENT -> {
            commentGroup
        }

        HistoryType.REPORT_CONTENT -> {
            reportGroup
        }

        HistoryType.LIKE -> {
            likeGroup
        }

        HistoryType.BOOKMARK -> {
            bookmarkGroup
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

fun List<CheckPoint>.toMetaCheckPoint(
    timestamp: Long = 0L
): MetaCheckPoint {
    return MetaCheckPoint(
        checkPointIdGroup = map { it.checkPointId },
        timeStamp = timestamp
    )
}

fun CheckPointAddRequest.toCheckpoint(
    userId: String,
    userName: String,
    checkPointId: String,
    imageName: String,
): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        userId = userId,
        userName = userName,
        imageName = imageName,
        latLng = latLng,
        description = description
    )
}

fun AuthProfile.toProfile(): Profile {
    return Profile(
        uid = uid,
        name = userName,
        hashMail = hashSha256(email),
        private = ProfilePrivate(
            mail = email,
            authCompany = authCompany.name,
            accountCreation = System.currentTimeMillis()
        )
    )
}