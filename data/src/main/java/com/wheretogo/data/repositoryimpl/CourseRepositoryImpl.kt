package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.LikeObject
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.datasource.LikeRemoteDatasource
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import com.wheretogo.data.model.route.RemoteRoute
import com.wheretogo.data.toCheckPointGroup
import com.wheretogo.data.toCourse
import com.wheretogo.data.toLocalCourse
import com.wheretogo.data.toMetaCheckPoint
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.repository.CourseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseRemoteDatasource: CourseRemoteDatasource,
    private val courseLocalDatasource: CourseLocalDatasource,
    private val routeRemoteDatasource: RouteRemoteDatasource,
    private val likeRemoteDatasource: LikeRemoteDatasource,
    private val checkPointRepository: CheckPointRepositoryImpl
) : CourseRepository {
    private val _cacheRouteGroup = mutableMapOf<String, List<LatLng>>()// id

    override suspend fun getCourse(courseId: String): Course? {
        return courseLocalDatasource.getCourse(courseId).run {
            courseRemoteDatasource.getCourse(courseId)?.run { // 저장된 코스 없을때 불러오기
                coroutineScope {
                    val checkPoint = async {
                        checkPointRepository.getCheckPointGroup(
                            remoteMetaCheckPoint.toMetaCheckPoint(
                                timestamp = 0L
                            )
                        )
                    }
                    val route = async {
                        _cacheRouteGroup.getOrPut(courseId) {
                            routeRemoteDatasource.getRouteInCourse(courseId).points
                        }
                    }

                    val like = async {
                        likeRemoteDatasource.getLikeInObject(
                            type = LikeObject.COURSE_LIKE,
                            objectId = courseId
                        ).like
                    }

                    this@run.toLocalCourse(
                        checkPoint = remoteMetaCheckPoint.copy(timeStamp = System.currentTimeMillis()), // 체크포인트 업데이트 시간 최신화
                        route = route.await(),
                        like = like.await(),
                    ).apply {
                        courseLocalDatasource.setCourse(this) // 불러온 코스 저장
                    }.toCourse(
                        checkPoints = checkPoint.await()
                    )
                }
            }
        }
    }

    override suspend fun getCourseGroupByGeoHash(geoHash: String): List<Course> {
        return if (courseLocalDatasource.isExistMetaGeoHash(geoHash)) { // geohash로 구역 호출 여부 확인
            courseLocalDatasource.getCourseGroupByGeoHash(geoHash)
        } else {
            courseRemoteDatasource.getCourseGroupByGeoHash(geoHash, "$geoHash\uf8ff") // 구역에 해당하는 코스들 가져오기
                .run {
                    courseLocalDatasource.setMetaGeoHash(
                        LocalMetaGeoHash(geoHash = geoHash, timestamp = System.currentTimeMillis())
                    )
                    coroutineScope {
                        map {
                            async {
                                val route =
                                    routeRemoteDatasource.getRouteInCourse(it.courseId).points
                                val checkPoint =
                                    DataMetaCheckPoint(checkPointIdGroup = it.remoteMetaCheckPoint.checkPointIdGroup)

                                it.toLocalCourse(
                                    route = route,
                                    checkPoint = checkPoint
                                ).apply {
                                    courseLocalDatasource.setCourse(this) // 불러온 코스 저장
                                }
                            }
                        }.awaitAll()
                    }
                }
        }.map {
            it.toCourse(checkPoints = it.localMetaCheckPoint.toMetaCheckPoint().toCheckPointGroup())
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
            checkPointRepository.setCheckPoint(it)
        }
    }

    override suspend fun updateMetaCheckPoint(
        courseId: String,
        metaCheckPoint: MetaCheckPoint
    ) {
        courseLocalDatasource
    }
}

