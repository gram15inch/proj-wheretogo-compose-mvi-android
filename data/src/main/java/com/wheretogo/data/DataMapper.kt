package com.wheretogo.data

import com.wheretogo.data.model.auth.DataSyncToken
import com.wheretogo.data.model.checkpoint.LocalCheckPoint
import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.comment.RemoteComment
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.course.LocalSnapshot
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.history.LocalHistory
import com.wheretogo.data.model.history.LocalHistoryGroupWrapper
import com.wheretogo.data.model.history.LocalHistoryIdGroup
import com.wheretogo.data.model.history.RemoteHistoryGroupWrapper
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.data.model.report.LocalReport
import com.wheretogo.data.model.report.RemoteReport
import com.wheretogo.data.model.route.LocalRoute
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.model.user.LocalProfile
import com.wheretogo.data.model.user.LocalProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePrivate
import com.wheretogo.data.model.user.RemoteProfilePublic
import com.wheretogo.data.model.user.RemoteSyncUser
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.auth.SyncToken
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointAddRequest
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentAddRequest
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseAddRequest
import com.wheretogo.domain.model.history.HistoryGroupWrapper
import com.wheretogo.domain.model.history.HistoryIdGroup
import com.wheretogo.domain.model.report.Report
import com.wheretogo.domain.model.report.ReportAddRequest
import com.wheretogo.domain.model.route.Route
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.util.Snapshot
import com.wheretogo.domain.toGeoHash

fun RemoteSyncUser.toProfileHistoryPair(): Pair<Profile, History> {
    val profile = publicUser.toProfile(privateUser)
    val history = history.toLocalHistory().toHistory()
    return profile to history
}

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

fun Profile.toProfilePublic(): RemoteProfilePublic {
    return RemoteProfilePublic(
        uid = uid,
        name = name,
        hashMail = hashMail,
    )
}

fun RemoteProfilePublic.toProfile(private: RemoteProfilePrivate? = null): Profile {
    return Profile(
        uid = uid,
        name = name,
        hashMail = hashMail
    ).run {
        if (private == null)
            this
        else
            copy(private = private.toProfilePrivate())
    }
}

fun RemoteProfilePublic.toLocalProfile(private: RemoteProfilePrivate): LocalProfile {
    return LocalProfile(
        uid = uid,
        name = name,
        hashMail = hashMail,
        private = private.toLocalProfilePrivate()
    )
}

fun RemoteProfilePrivate.toProfilePrivate(): ProfilePrivate {
    return ProfilePrivate(
        mail = mail,
        authCompany = authCompany,
        lastVisited = lastVisited,
        accountCreation = accountCreation,
        isAdRemove = adRemove,
        isAdmin = admin
    )
}

fun RemoteProfilePrivate.toLocalProfilePrivate(): LocalProfilePrivate {
    return LocalProfilePrivate(
        mail = mail,
        authCompany = authCompany,
        lastVisited = lastVisited,
        accountCreation = accountCreation,
        isAdRemove = adRemove,
        isAdmin = admin
    )
}

fun ProfilePrivate.toRemoteProfilePrivate(): RemoteProfilePrivate {
    return RemoteProfilePrivate(
        mail = mail,
        authCompany = authCompany,
        lastVisited = lastVisited,
        accountCreation = accountCreation,
        adRemove = isAdRemove,
        admin = isAdmin
    )
}

fun ReportAddRequest.toRemoteReport(reportId: String): RemoteReport {
    return RemoteReport(
        reportId = reportId,
        type = type.toDataReportType(),
        userId = userId,
        contentId = contentId,
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = status.name,
        createAt = System.currentTimeMillis()
    )
}

fun RemoteReport.toLocalReport(): LocalReport {
    return LocalReport(
        reportId = reportId,
        type = type,
        userId = userId,
        contentId = contentId,
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = status,
        createAt = createAt,
        timestamp = System.currentTimeMillis()
    )
}

fun LocalReport.toReport(): Report {
    return Report(
        reportId = reportId,
        type = type.toReportType(),
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

fun RemoteRoute.toLocalRoute(): LocalRoute {
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
        type = type.toReportType(),
        userId = userId,
        contentId = contentId,
        targetUserId = targetUserId,
        targetUserName = targetUserName,
        reason = reason,
        status = ReportStatus.valueOf(status),
        createAt = createAt
    )
}

fun ReportType.toDataReportType(): DataReportType{
    return when(this){
        ReportType.USER -> DataReportType.USER
        ReportType.COURSE -> DataReportType.COURSE
        ReportType.COMMENT -> DataReportType.COMMENT
        ReportType.CHECKPOINT -> DataReportType.CHECKPOINT
    }
}

fun DataReportType.toReportType(): ReportType{
    return when(this){
        DataReportType.USER -> ReportType.USER
        DataReportType.COURSE -> ReportType.COURSE
        DataReportType.COMMENT -> ReportType.COMMENT
        DataReportType.CHECKPOINT -> ReportType.CHECKPOINT
    }
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

fun CheckPointAddRequest.toRemoteCheckPoint(
    checkPointId: String
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
        userId = userId,
        userName = userName,
        latLng = latLng,
        captionId = captionId,
        caption = caption,
        imageId = imageId,
        description = description,
        timestamp = System.currentTimeMillis()
    )
}

fun LocalCheckPoint.toCheckPoint(imageLocalPath: String = ""): CheckPoint {
    return CheckPoint(
        checkPointId = checkPointId,
        courseId = courseId,
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
): RemoteCourse {
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

fun LocalCourse.toCourse(): Course {
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
    keyword: List<String> = emptyList()
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

fun List<DataLatLng>.toLatLngGroup(): List<LatLng> {
    return map { it.toLatLng() }
}

fun List<LatLng>.toDataLatLngGroup(): List<DataLatLng> {
    return map { it.toDataLatLng() }
}

fun LocalHistoryGroupWrapper.toHistoryGroupWrapper(): HistoryGroupWrapper {
    return HistoryGroupWrapper(
        type = type.toHistoryType(),
        historyIdGroup = HistoryIdGroup(
            groupById = historyIdGroup.groupById
        ),
        lastAddedAt = lastAddedAt
    )
}

fun HistoryGroupWrapper.toLocalHistoryGroupWrapper(): LocalHistoryGroupWrapper {
    return LocalHistoryGroupWrapper(
        type = type.toDataHistoryType(),
        historyIdGroup = LocalHistoryIdGroup(
            groupById = historyIdGroup.groupById
        ),
        lastAddedAt = lastAddedAt
    )
}

fun Map<String, List<String>>.toLocalHistoryIdGroup(): LocalHistoryIdGroup {
    return LocalHistoryIdGroup(this.mapValues {
        it.value.toHashSet()
    })
}

fun HistoryIdGroup.toRemoteHistoryIdGroup(): Map<String, List<String>> {
    return this.groupById.mapValues { it.value.toList() }
}

fun RemoteHistoryGroupWrapper.toLocalHistoryGroupWrapper(): LocalHistoryGroupWrapper {
    return LocalHistoryGroupWrapper(
        type = type,
        historyIdGroup = historyIdGroup.toLocalHistoryIdGroup(),
        lastAddedAt = lastAddedAt
    )
}

fun HistoryGroupWrapper.toRemoteHistoryGroupWrapper(): RemoteHistoryGroupWrapper {
    return RemoteHistoryGroupWrapper(
        type = type.toDataHistoryType(),
        historyIdGroup = historyIdGroup.toRemoteHistoryIdGroup(),
        lastAddedAt = lastAddedAt
    )
}

fun AuthCompany.toDataAuthCompany(): DataAuthCompany{
    return when(this){
        AuthCompany.PROFILE -> DataAuthCompany.PROFILE
        AuthCompany.GOOGLE -> DataAuthCompany.GOOGLE
    }
}

fun DataAuthCompany.toAuthCompany(): AuthCompany{
    return when(this){
        DataAuthCompany.PROFILE -> AuthCompany.PROFILE
        DataAuthCompany.GOOGLE -> AuthCompany.GOOGLE
    }
}


fun HistoryType.toDataHistoryType(): DataHistoryType{
    return when(this){
        HistoryType.COURSE -> DataHistoryType.COURSE
        HistoryType.CHECKPOINT -> DataHistoryType.CHECKPOINT
        HistoryType.COMMENT -> DataHistoryType.COMMENT
        HistoryType.LIKE -> DataHistoryType.LIKE
        HistoryType.REPORT -> DataHistoryType.REPORT
    }
}

fun DataHistoryType.toHistoryType(): HistoryType{
    return when(this){
        DataHistoryType.COURSE -> HistoryType.COURSE
        DataHistoryType.CHECKPOINT -> HistoryType.CHECKPOINT
        DataHistoryType.COMMENT -> HistoryType.COMMENT
        DataHistoryType.LIKE -> HistoryType.LIKE
        DataHistoryType.REPORT -> HistoryType.REPORT
    }
}

fun List<RemoteHistoryGroupWrapper>.toLocalHistory(): LocalHistory {
    var history = LocalHistory()
    forEach {
        history = when (it.type) {
            DataHistoryType.COURSE -> history.copy(
                course = it.toLocalHistoryGroupWrapper()
            )

            DataHistoryType.CHECKPOINT -> history.copy(
                checkpoint = it.toLocalHistoryGroupWrapper()
            )

            DataHistoryType.COMMENT -> history.copy(
                comment = it.toLocalHistoryGroupWrapper()
            )

            DataHistoryType.LIKE -> history.copy(
                like = it.toLocalHistoryGroupWrapper()
            )

            DataHistoryType.REPORT -> history.copy(
                report = it.toLocalHistoryGroupWrapper()
            )
        }
    }
    return history
}

fun LocalHistory.toHistory(): History {
    return History(
        course = course.toHistoryGroupWrapper(),
        checkpoint = checkpoint.toHistoryGroupWrapper(),
        comment = comment.toHistoryGroupWrapper(),
        like = like.toHistoryGroupWrapper(),
        bookmark = bookmark.toHistoryGroupWrapper(),
        report = report.toHistoryGroupWrapper(),
    )
}


fun History.toHistory(): LocalHistory {
    return LocalHistory(
        course = course.toLocalHistoryGroupWrapper(),
        checkpoint = checkpoint.toLocalHistoryGroupWrapper(),
        comment = comment.toLocalHistoryGroupWrapper(),
        like = like.toLocalHistoryGroupWrapper(),
        bookmark = bookmark.toLocalHistoryGroupWrapper(),
        report = report.toLocalHistoryGroupWrapper(),
    )
}

fun History.remoteGroupWrapper(): List<RemoteHistoryGroupWrapper> {
    return listOf(
        course.toRemoteHistoryGroupWrapper(),
        checkpoint.toRemoteHistoryGroupWrapper(),
        comment.toRemoteHistoryGroupWrapper(),
        like.toRemoteHistoryGroupWrapper(),
        bookmark.toRemoteHistoryGroupWrapper(),
        report.toRemoteHistoryGroupWrapper(),
    )
}

fun SyncToken.toDataSyncToken(): DataSyncToken{
    return DataSyncToken(
        authCompany = authCompany.toDataAuthCompany(),
        token = token
    )
}