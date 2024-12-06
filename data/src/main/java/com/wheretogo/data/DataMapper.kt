package com.wheretogo.data

import com.wheretogo.data.model.checkpoint.RemoteCheckPoint
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.LocalCourse
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.map.DataLatLng
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.toGeoHash


fun DataMetaCheckPoint.toMetaCheckPoint(): MetaCheckPoint {
    return MetaCheckPoint(metaCheckPointGroup = dataGroup, timeStamp = timeStamp)
}

fun CheckPoint.toRemoteCheckPoint(): RemoteCheckPoint {
    return RemoteCheckPoint(
        checkPointId = checkPointId,
        latLng = latLng.toDataLatLng(),
        titleComment = titleComment,
        imgUrl = imgUrl
    )
}

fun LocalCourse.toDomainCourse(
    route: List<LatLng> = this.route,
    like: Int = this.like
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        waypoints = waypoints,
        route = route,
        metaCheckPoint = metaCheckPoint.toMetaCheckPoint(),
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}

fun Course.toLocalCourse(): LocalCourse {
    return LocalCourse(
        courseId = courseId,
        courseName = courseName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        route = route,
        metaCheckPoint = metaCheckPoint.toDataMetaCheckPoint(),
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
        metaCheckPoint = metaCheckPoint,
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}

fun Course.toRemoteCourse(): RemoteCourse {
    return RemoteCourse(
        courseId = courseId,
        courseName = courseName,
        latitude = cameraLatLng.latitude,
        longitude = cameraLatLng.longitude,
        geoHash = cameraLatLng.toGeoHash(6),
        waypoints = waypoints,
        metaCheckPoint = metaCheckPoint.toDataMetaCheckPoint(),
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom
    )
}

fun RemoteCourse.toDomainCourse(
    route: List<LatLng> = emptyList(),
    like: Int = 0
): Course {
    return Course(
        courseId = courseId,
        courseName = courseName,
        waypoints = waypoints,
        route = route,
        metaCheckPoint = metaCheckPoint.toMetaCheckPoint(),
        duration = duration,
        tag = tag,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng,
        zoom = zoom,
        like = like
    )
}

fun MetaCheckPoint.toDataMetaCheckPoint(): DataMetaCheckPoint {
    return DataMetaCheckPoint(
        dataGroup = metaCheckPointGroup,
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