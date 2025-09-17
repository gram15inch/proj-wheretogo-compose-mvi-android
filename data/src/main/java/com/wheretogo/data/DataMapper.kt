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
import com.wheretogo.domain.model.report.Report
import com.wheretogo.domain.model.report.ReportAddRequest
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentAddRequest
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.model.course.CourseAddRequest
import com.wheretogo.domain.model.util.Snapshot
import com.wheretogo.domain.model.route.Route
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

fun RemoteProfilePublic.toLocalProfile(private: RemoteProfilePrivate): LocalProfile{
    return LocalProfile(
        uid = uid,
        name = name,
        hashMail = hashMail,
        private = private.toLocalProfilePrivate()
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

fun RemoteProfilePrivate.toLocalProfilePrivate():LocalProfilePrivate{
    return LocalProfilePrivate(
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

fun ReportAddRequest.toRemoteReport(reportId:String): RemoteReport{
    return RemoteReport(
        reportId= reportId,
        type = type.name,
        userId = userId,
        contentId=contentId,
        targetUserId= targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = status.name,
        createAt = System.currentTimeMillis()
    )
}

fun RemoteReport.toLocalReport(): LocalReport{
    return LocalReport(
        reportId= reportId,
        type = type,
        userId = userId,
        contentId=contentId,
        targetUserId= targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = status,
        createAt = createAt,
        timestamp = System.currentTimeMillis()
    )
}

fun LocalReport.toReport():Report {
    return Report(
        reportId = reportId,
        type = ReportType.valueOf(type),
        userId = userId,
        contentId = contentId,
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = ReportStatus.valueOf(status),
        createAt = createAt,
        timestamp = timestamp
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

fun RemoteRoute.toLocalRoute(): LocalRoute {
    return LocalRoute(
        courseId = courseId,
        points = points,
        duration = duration,
        distance = distance
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
        userId = userId,
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
        createAt = createAt
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
        like = like,
        isFocus = isFocus,
        createAt = createAt,
        timestamp = System.currentTimeMillis()
    )
}

fun List<RemoteComment>?.toCommentGroup(): List<Comment> {
    return this?.map { it.toComment() } ?: emptyList()
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
        like = like,
        isFocus = isFocus,
        createAt = createAt
    )
}

fun CommentAddRequest.toComment(commentId: String): Comment {
    return Comment(
        commentId = commentId,
        groupId = content.groupId,
        userId = profile.uid,
        userName = profile.name,
        emoji = content.emoji,
        oneLineReview = content.oneLineReview,
        detailedReview = content.detailedReview,
        like = content.like,
        isUserCreated = true,
        isUserLiked = false,
        isFocus = false,
        createAt = System.currentTimeMillis(),
        timestamp = 0
    )
}

fun LocalSnapshot.toSnapshot(): Snapshot {
    return Snapshot(
        indexIdGroup = indexIdGroup,
        refId = refId,
        updateAt = updateAt
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
        imageId = imageId,
        description = description
    )
}

fun CheckPointAddRequest.toRemoteCheckPoint(
    checkPointId:String
): RemoteCheckPoint {
    return RemoteCheckPoint(
        checkPointId = checkPointId,
        courseId = content.courseId,
        userId = profile.uid,
        userName = profile.name,
        latLng = content.latLng.toDataLatLng(),
        caption = "",
        imageId = image.imageId,
        description = content.description
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
        imageId = imageId,
        description = description,
        timestamp = System.currentTimeMillis()
    )
}

fun LocalCheckPoint.toCheckPoint(imageLocalPath:String=""): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        courseId= courseId,
        userId = userId,
        userName = userName,
        latLng = latLng.toLatLng(),
        caption = caption,
        imageId = imageId,
        thumbnail = imageLocalPath,
    )
}

@JvmName("fromRemoteCheckPointToLocal")
fun List<RemoteCheckPoint>.toLocal() = map { it.toLocalCheckPoint() }
@JvmName("fromLocalCheckPointToDomain")
fun List<LocalCheckPoint>.toDomain() = map { it.toCheckPoint() }

fun CourseAddRequest.toCourse(
    courseId: String,
): RemoteCourse{
    return RemoteCourse(
        courseId = courseId,
        courseName = content.courseName,
        userId = profile.uid,
        userName = profile.name,
        latitude = content.cameraLatLng.latitude,
        longitude = content.cameraLatLng.longitude,
        geoHash = content.cameraLatLng.toGeoHash(6),
        waypoints = content.waypoints.toDataLatLngGroup(),
        keyword = keyword,
        duration = content.duration,
        type = content.type,
        level = content.level,
        relation = content.relation,
        cameraLatLng = content.cameraLatLng.toDataLatLng(),
        zoom = content.zoom,
        createAt = System.currentTimeMillis()
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

fun LocalCourse.toCourse(): Course{
    return Course(
        courseId = courseId,
        courseName = courseName,
        userId = userId,
        userName = userName,
        waypoints = waypoints.toLatLngGroup(),
        checkpointIdGroup = checkpointSnapshot.indexIdGroup,
        points = emptyList(),
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng.toLatLng(),
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
        updateAt = updateAt
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
