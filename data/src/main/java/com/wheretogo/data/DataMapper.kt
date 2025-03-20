package com.wheretogo.data

import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.report.LocalReport
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.data.model.route.LocalRoute
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.model.user.ProfilePublic
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.model.map.Route
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.toGeoHash



fun Profile.toProfilePublic():ProfilePublic{
    return ProfilePublic(
        uid = uid,
        name = name,
        hashMail = hashMail,
    )
}

fun ProfilePublic.toProfile():Profile{
    return Profile(
        uid = uid,
        name = name,
        hashMail = hashMail
    )
}

fun Report.toLocalReport():LocalReport{
    return LocalReport(
        reportId= reportId,
        type = type.name,
        userId = userId,
        contentId=contentId,
        targetUserId= targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = status.name,
        timestamp = timestamp
    )
}

fun LocalReport.toReport():Report{
    return Report(
        reportId= reportId,
        type = ReportType.valueOf(type),
        userId = userId,
        contentId=contentId,
        targetUserId= targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = ReportStatus.valueOf(status),
        timestamp = timestamp
    )
}


fun Route.toLocalRoute(): LocalRoute {
    return LocalRoute(
        courseId = courseId,
        points = points,
        duration = duration,
        distance = distance
    )
}


fun LocalRoute.toRoute(): Route {
    return Route(
        courseId = courseId,
        points = points,
        duration = duration,
        distance = distance
    )
}

fun Route.toRemoteRoute(): RemoteRoute {
    return RemoteRoute(
        courseId = courseId,
        points = points,
        duration = duration,
        distance = distance
    )
}

fun RemoteRoute.toRoute(): Route {
    return Route(
        courseId = courseId,
        points = points,
        duration = duration,
        distance = distance
    )
}

fun Report.toRemoteReport(): RemoteReport {
    return RemoteReport(
        reportId = reportId,
        type = type.name,
        userId = userId,
        contentId = contentId,
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = status.name,
        timestamp = timestamp
    )
}

fun RemoteReport.toReport(): Report {
    return Report(
        reportId = reportId,
        type = ReportType.valueOf(type),
        userId = userId,
        contentId = contentId,
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = ReportStatus.valueOf(status),
        timestamp = timestamp
    )
}

fun RemoteComment.toComment(): Comment {
    return Comment(
        commentId = commentId,
        userId = userId,
        userName = userName,
        groupId = commentGroupId,
        emoji = emoji,
        oneLineReview = oneLineReview,
        detailedReview = detailedReview,
        date = date,
        like = like,
        timestamp = timestamp
    )
}

fun Comment.toRemoteComment(): RemoteComment {
    return RemoteComment(
        commentId = commentId,
        commentGroupId = groupId,
        userId = userId,
        userName = userName,
        emoji = emoji,
        oneLineReview = oneLineReview,
        detailedReview = detailedReview,
        date = date,
        like = like,
        timestamp = timestamp
    )
}

fun DataMetaCheckPoint.toMetaCheckPoint(timestamp: Long = timeStamp): MetaCheckPoint {
    return MetaCheckPoint(checkPointIdGroup = checkPointIdGroup, timeStamp = timestamp)
}

fun CheckPoint.toRemoteCheckPoint(): RemoteCheckPoint {
    return RemoteCheckPoint(
        checkPointId = checkPointId,
        userId = userId,
        userName = userName,
        latLng = latLng.toDataLatLng(),
        titleComment = titleComment,
        imageName = imageName,
        description = description
    )
}

fun CheckPoint.toLocalCheckPoint(): LocalCheckPoint {
    return LocalCheckPoint(
        checkPointId = checkPointId,
        userId = userId,
        userName = userName,
        latLng = latLng,
        titleComment = titleComment,
        imageName = imageName,
        imageLocalPath = this.imageLocalPath,
        description = description,
        timestamp = System.currentTimeMillis()
    )
}

fun RemoteCheckPoint.toCheckPoint(): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        userId = userId,
        userName = userName,
        latLng = latLng.toLatLng(),
        titleComment = titleComment,
        imageName = imageName,
        description = description
    )
}

fun RemoteCheckPoint.toLocalCheckPoint(
    localImgUrl: String = "",
    timestamp: Long = 0L
): LocalCheckPoint {
    return LocalCheckPoint(
        checkPointId = checkPointId,
        userId= userId,
        userName= userName,
        latLng = latLng.toLatLng(),
        titleComment = titleComment,
        imageName = imageName,
        imageLocalPath = localImgUrl,
        description = description,
        timestamp = timestamp
    )
}

fun LocalCheckPoint.toCheckPoint(): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        userId=userId,
        userName = userName,
        latLng = latLng,
        titleComment = titleComment,
        imageName = imageName,
        imageLocalPath = imageLocalPath,
    )
}


fun LocalCourse.toCourse(
    points: List<LatLng> = emptyList(),
    like: Int = this.like
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        waypoints = waypoints,
        checkpointIdGroup = localMetaCheckPoint.checkPointIdGroup,
        points = points,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}

fun Course.toLocalCourse(
    checkPoint: DataMetaCheckPoint = DataMetaCheckPoint()
): LocalCourse {
    return LocalCourse(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        localMetaCheckPoint = checkPoint,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}


fun RemoteCourse.toLocalCourse(
    checkPoint: DataMetaCheckPoint = DataMetaCheckPoint(checkPointIdGroup = this.dataMetaCheckPoint.checkPointIdGroup),
    like: Int = 0
): LocalCourse {
    return LocalCourse(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        localMetaCheckPoint = checkPoint,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like,
    )
}

fun Course.toRemoteCourse(
    checkPoint: DataMetaCheckPoint = DataMetaCheckPoint(),
): RemoteCourse {
    return RemoteCourse(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        dataMetaCheckPoint = checkPoint,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom
    )
}

fun RemoteCourse.toCourse(
    points: List<LatLng> = emptyList(),
    like: Int = 0
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        waypoints = waypoints,
        checkpointIdGroup = dataMetaCheckPoint.checkPointIdGroup,
        points = points,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}

fun MetaCheckPoint.toDataMetaCheckPoint(timeStamp: Long = this.timeStamp): DataMetaCheckPoint {
    return DataMetaCheckPoint(
        checkPointIdGroup = checkPointIdGroup,
        timeStamp = timeStamp
    )
}

fun DataLatLng.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun LatLng.toDataLatLng(): DataLatLng {
    return DataLatLng(this.latitude, this.longitude)
}
