package com.wheretogo.data

import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.course.LocalSnapshot
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.report.LocalReport
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.data.model.route.LocalRoute
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.data.model.user.LocalProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.Snapshot
import com.wheretogo.domain.model.map.Route
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.toGeoHash


fun Profile.toLocalProfile(): LocalProfile {
    return LocalProfile(
        uid = uid,
        name = name,
        hashMail = hashMail,
        private = private.toLocalProfilePrivate()
    )
}

fun LocalProfile.toProfile(): Profile {
    return Profile(
        uid = uid,
        name = name,
        hashMail = hashMail,
        private = private.toProfilePrivate()
    )
}

fun ProfilePrivate.toLocalProfilePrivate(): LocalProfilePrivate {
    return LocalProfilePrivate(
        mail = mail,
        authCompany = authCompany,
        lastVisited = lastVisited,
        accountCreation = accountCreation,
        isAdRemove = isAdRemove,
        isAdmin = isAdmin
    )
}

fun LocalProfilePrivate.toProfilePrivate(): ProfilePrivate {
    return ProfilePrivate(
        mail = mail,
        authCompany = authCompany,
        lastVisited = lastVisited,
        accountCreation = accountCreation,
        isAdRemove = isAdRemove,
        isAdmin = isAdmin
    )
}

fun Profile.toProfilePublic():RemoteProfilePublic{
    return RemoteProfilePublic(
        uid = uid,
        name = name,
        hashMail = hashMail,
    )
}

fun RemoteProfilePublic.toProfile():Profile{
    return Profile(
        uid = uid,
        name = name,
        hashMail = hashMail
    )
}

fun RemoteProfilePrivate.toProfilePrivate():ProfilePrivate{
    return ProfilePrivate(
        mail = mail,
        authCompany = authCompany,
        lastVisited = lastVisited,
        accountCreation = accountCreation,
        isAdRemove = isAdRemove,
        isAdmin = isAdmin
    )
}

fun ProfilePrivate.toRemoteProfilePrivate():RemoteProfilePrivate{
    return RemoteProfilePrivate(
        mail = mail,
        authCompany = authCompany,
        lastVisited = lastVisited,
        accountCreation = accountCreation,
        isAdRemove = isAdRemove,
        isAdmin = isAdmin
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
        points = points.toDataLatLngGroup(),
        duration = duration,
        distance = distance
    )
}


fun LocalRoute.toRoute(): Route {
    return Route(
        courseId = courseId,
        points = points.toLatLngGroup(),
        duration = duration,
        distance = distance
    )
}

fun Route.toRemoteRoute(): RemoteRoute {
    return RemoteRoute(
        courseId = courseId,
        points = points.toDataLatLngGroup(),
        duration = duration,
        distance = distance
    )
}

fun RemoteRoute.toRoute(): Route {
    return Route(
        courseId = courseId,
        points = points.toLatLngGroup(),
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

fun LocalSnapshot.toSnapshot(): Snapshot {
    return Snapshot(
        indexIdGroup = indexIdGroup,
        refId = refId,
        timeStamp = timeStamp
    )
}

fun CheckPoint.toRemoteCheckPoint(): RemoteCheckPoint {
    return RemoteCheckPoint(
        checkPointId = checkPointId,
        courseId = courseId,
        userId = userId,
        userName = userName,
        latLng = latLng.toDataLatLng(),
        caption = caption,
        imageName = imageName,
        description = description
    )
}

fun CheckPoint.toLocalCheckPoint(): LocalCheckPoint {
    return LocalCheckPoint(
        checkPointId = checkPointId,
        courseId = courseId,
        userId = userId,
        userName = userName,
        latLng = latLng.toDataLatLng(),
        caption = caption,
        imageName = imageName,
        imageLocalPath = this.imageLocalPath,
        description = description,
        timestamp = System.currentTimeMillis()
    )
}

fun RemoteCheckPoint.toCheckPoint(): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        courseId = courseId,
        userId = userId,
        userName = userName,
        latLng = latLng.toLatLng(),
        captionId = captionId,
        caption = caption,
        imageName = imageName,
        description = description
    )
}

fun RemoteCheckPoint.toLocalCheckPoint(): LocalCheckPoint {
    return LocalCheckPoint(
        checkPointId = checkPointId,
        courseId = courseId,
        userId= userId,
        userName= userName,
        latLng = latLng,
        captionId = captionId,
        caption = caption,
        imageName = imageName,
        imageLocalPath = imageName,
        description = description,
        timestamp = System.currentTimeMillis()
    )
}

fun LocalCheckPoint.toCheckPoint(): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        courseId= courseId,
        userId = userId,
        userName = userName,
        latLng = latLng.toLatLng(),
        caption = caption,
        imageName = imageName,
        imageLocalPath = imageLocalPath,
    )
}

@JvmName("fromRemoteCheckPointToLocal")
fun List<RemoteCheckPoint>.toLocal() = map { it.toLocalCheckPoint() }
@JvmName("fromRemoteCheckPointToDomain")
fun List<RemoteCheckPoint>.toDomain() = map { it.toCheckPoint() }
@JvmName("fromLocalCheckPointToDomain")
fun List<LocalCheckPoint>.toDomain() = map { it.toCheckPoint() }


fun LocalCourse.toCourse(
    points: List<LatLng> = emptyList(),
    like: Int = this.like
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        waypoints = waypoints.toLatLngGroup(),
        checkpointIdGroup = checkpointSnapshot.indexIdGroup,
        points = points,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng.toLatLng(),
        zoom = zoom,
        like = like
    )
}

fun Course.toLocalCourse(
    checkPoint: LocalSnapshot = LocalSnapshot()
): LocalCourse {
    return LocalCourse(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints.toDataLatLngGroup(),
        checkpointSnapshot = checkPoint,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng.toDataLatLng(),
        zoom = zoom,
        like = like
    )
}


fun RemoteCourse.toLocalCourse(): LocalCourse {
    return LocalCourse(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toLatLng().toGeoHash(6),
        waypoints = waypoints,
        checkpointSnapshot = LocalSnapshot(refId = courseId),
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = 0,
    )
}

fun Course.toRemoteCourse(
    keyword:List<String> = emptyList()
): RemoteCourse {
    return RemoteCourse(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints.toDataLatLngGroup(),
        keyword = keyword,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng.toDataLatLng(),
        zoom = zoom
    )
}

fun RemoteCourse.toCourse(): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        waypoints = waypoints.toLatLngGroup(),
        checkpointIdGroup = emptyList(),
        points = emptyList(),
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng.toLatLng(),
        zoom = zoom,
        like = 0
    )
}

fun Snapshot.toLocalSnapshot(): LocalSnapshot {
    return LocalSnapshot(
        refId = refId,
        indexIdGroup = indexIdGroup,
        timeStamp = timeStamp
    )
}

fun DataLatLng.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun LatLng.toDataLatLng(): DataLatLng {
    return DataLatLng(this.latitude, this.longitude)
}

fun List<DataLatLng>.toLatLngGroup():List<LatLng>{
    return map { it.toLatLng() }
}
fun List<LatLng>.toDataLatLngGroup():List<DataLatLng>{
    return map { it.toDataLatLng() }
}
