package com.wheretogo.data

import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.model.map.Route
import com.wheretogo.domain.toGeoHash


fun Route.toRoute(): RemoteRoute {
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
        latLng = latLng.toDataLatLng(),
        titleComment = titleComment,
        imgUrl = remoteImgUrl
    )
}

fun CheckPoint.toLocalCheckPoint(
    localImgUrl: String = this.localImgUrl,
): LocalCheckPoint {
    return LocalCheckPoint(
        checkPointId = checkPointId,
        latLng = latLng,
        titleComment = titleComment,
        remoteImgUrl = remoteImgUrl,
        localImgUrl = localImgUrl
    )
}

fun RemoteCheckPoint.toCheckPoint(): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        latLng = latLng.toLatLng(),
        titleComment = titleComment,
        remoteImgUrl = imgUrl
    )
}

fun RemoteCheckPoint.toLocalCheckPoint(
    localImgUrl: String = "",
    timestamp: Long = 0L
): LocalCheckPoint {
    return LocalCheckPoint(
        checkPointId = checkPointId,
        latLng = latLng.toLatLng(),
        titleComment = titleComment,
        remoteImgUrl = imgUrl,
        localImgUrl = localImgUrl,
        timestamp = timestamp
    )
}

fun LocalCheckPoint.toCheckPoint(): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        latLng = latLng,
        titleComment = titleComment,
        remoteImgUrl = remoteImgUrl,
        localImgUrl = localImgUrl,
    )
}


fun LocalCourse.toCourse(
    route: List<LatLng> = this.route,
    checkPoints: List<CheckPoint> = this.localMetaCheckPoint.toMetaCheckPoint().toCheckPointGroup(),
    like: Int = this.like
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        waypoints = waypoints,
        points = route,
        checkpoints = checkPoints,
        duration = duration,
        tag = tag,
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
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        route = points,
        localMetaCheckPoint = checkPoint,
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}


fun RemoteCourse.toLocalCourse(
    route: List<LatLng> = emptyList(),
    checkPoint: DataMetaCheckPoint = DataMetaCheckPoint(checkPointIdGroup = this.remoteMetaCheckPoint.checkPointIdGroup),
    like: Int = 0
): LocalCourse {
    return LocalCourse(
        courseId = courseId,
        courseName = courseName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        route = route,
        localMetaCheckPoint = checkPoint,
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}

fun Course.toRemoteCourse(
    checkPoint: DataMetaCheckPoint = DataMetaCheckPoint(),
): RemoteCourse {
    return RemoteCourse(
        courseId = courseId,
        courseName = courseName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        remoteMetaCheckPoint = checkPoint,
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom
    )
}

fun RemoteCourse.toCourse(
    route: List<LatLng> = emptyList(),
    checkPoint: List<CheckPoint> = emptyList(),
    like: Int = 0
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        waypoints = waypoints,
        points = route,
        checkpoints = checkPoint,
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}

fun List<LocalCheckPoint>.toCheckPoint(): List<CheckPoint> {
    return map {
        CheckPoint(
            checkPointId = it.checkPointId,
            latLng = it.latLng,
            titleComment = it.titleComment,
            remoteImgUrl = it.remoteImgUrl,
            localImgUrl = it.localImgUrl
        )
    }
}

fun List<CheckPoint>.toLocalCheckPoint(): List<LocalCheckPoint> {
    return map {
        LocalCheckPoint(
            checkPointId = it.checkPointId,
            latLng = it.latLng,
            titleComment = it.titleComment,
            remoteImgUrl = it.remoteImgUrl,
            localImgUrl = it.localImgUrl
        )
    }
}

fun MetaCheckPoint.toCheckPointGroup(): List<CheckPoint> {
    return checkPointIdGroup.map { CheckPoint(checkPointId = it) }
}

fun MetaCheckPoint.toLocalCheckPointGroup(): List<LocalCheckPoint> {
    return checkPointIdGroup.map { LocalCheckPoint(checkPointId = it) }
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

fun List<DataLatLng>.toLatlngList(): List<LatLng> {
    return this.map { it.toLatLng() }
}


fun LatLng.toDataLatLng(): DataLatLng {
    return DataLatLng(this.latitude, this.longitude)
}

fun List<LatLng>.toDataLatlngList(): List<DataLatLng> {
    return this.map { it.toDataLatLng() }
}