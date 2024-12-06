package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.LikeObject
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.datasource.LikeRemoteDatasource
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.toDomainCourse
import com.wheretogo.data.toLocalCourse
import com.wheretogo.data.toRemoteCheckPoint
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.repository.CourseRepository
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseRemoteDatasource: CourseRemoteDatasource,
    private val courseLocalDatasource: CourseLocalDatasource,
    private val routeRemoteDatasource: RouteRemoteDatasource,
    private val likeRemoteDatasource: LikeRemoteDatasource,
    private val checkPointRemoteDatasource: CheckPointRemoteDatasource,
    private val checkPointLocalDatasource: CheckPointLocalDatasource
) : CourseRepository {

    override suspend fun getCourse(courseId: String): Course? {
        val dataCourse = courseLocalDatasource.getCourse(courseId)

        return if (dataCourse != null) {
            val like = likeRemoteDatasource
                .getLikeInObject(type = LikeObject.COURSE_LIKE, objectId = dataCourse.courseId)
                .like
            val route = dataCourse.route.run {
                ifEmpty { routeRemoteDatasource.getRouteInCourse(courseId).points }
            }
            dataCourse.toDomainCourse(route = route, like = like)
        } else {
            null
        }
    }

    override suspend fun getCourseGroupByGeoHash(geoHash: String): List<Course> {
        return if (courseLocalDatasource.isExistMetaGeoHash(geoHash)) {
            courseLocalDatasource.getCourseGroupByGeoHash(geoHash).map { it.toDomainCourse() }
        } else {
            courseRemoteDatasource.getCourseGroupByGeoHash(geoHash, "$geoHash\uf8ff")
                .run {
                    courseLocalDatasource.setMetaGeoHash(
                        LocalMetaGeoHash(geoHash = geoHash, timestamp = System.currentTimeMillis())
                    )

                    map {
                        val route = routeRemoteDatasource.getRouteInCourse(it.courseId).points
                        courseLocalDatasource.setCourse(it.toLocalCourse(route = route))
                        it.toDomainCourse(route = route)
                    }
                }
        }
    }

    override suspend fun setCourse(
        course: Course,
        checkPoints: List<CheckPoint>
    ) {
        courseRemoteDatasource.setCourse(course.toRemoteCourse())
        courseLocalDatasource.setCourse(course.toLocalCourse())

        if (course.route.isNotEmpty()) {
            routeRemoteDatasource.setRouteInCourse(
                RemoteRoute(
                    courseId = course.courseId,
                    points = course.route
                )
            )
        }

        checkPoints.forEach {
            checkPointRemoteDatasource.setCheckPoint(it.toRemoteCheckPoint())
            //checkPointLocalDatasource.setCheckPoint(LocalCheckPoint())
        }
    }

}

