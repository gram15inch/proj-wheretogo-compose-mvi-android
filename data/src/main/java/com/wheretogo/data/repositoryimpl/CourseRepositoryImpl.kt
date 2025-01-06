package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import com.wheretogo.data.toCourse
import com.wheretogo.data.toDataMetaCheckPoint
import com.wheretogo.data.toLocalCourse
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
    private val routeRemoteDatasource: RouteRemoteDatasource
) : CourseRepository {
    private val _cacheRouteGroup = mutableMapOf<String, List<LatLng>>()// id

    override suspend fun getCourse(courseId: String): Course? {
        val course = courseLocalDatasource.getCourse(courseId)?.toCourse() ?: run {
            courseRemoteDatasource.getCourse(courseId)?.run {
                this.toLocalCourse(route = routeRemoteDatasource.getRouteInCourse(courseId).points)
            }?.toCourse()
        }
        return course
    }

    override suspend fun getCourseGroupByGeoHash(geoHash: String): List<Course> {
        return if (courseLocalDatasource.isExistMetaGeoHash(geoHash)) { // geohash로 구역 호출 여부 확인
            courseLocalDatasource.getCourseGroupByGeoHash(geoHash)
        } else {
            courseRemoteDatasource.getCourseGroupByGeoHash(
                geoHash,
                "$geoHash\uf8ff"
            ) // 구역에 해당하는 코스들 가져오기
                .run {
                    courseLocalDatasource.setMetaGeoHash(
                        LocalMetaGeoHash(geoHash = geoHash, timestamp = System.currentTimeMillis())
                    )
                    coroutineScope {
                        map {
                            async {
                                val route = _cacheRouteGroup.getOrPut(it.courseId) {
                                    routeRemoteDatasource.getRouteInCourse(it.courseId).points
                                }

                                //체크포인트 아이디만 저장
                                it.toLocalCourse(
                                    route = route
                                ).apply {
                                    courseLocalDatasource.setCourse(this) // 불러온 코스 저장
                                }
                            }
                        }.awaitAll()
                    }
                }
        }.map {
            it.toCourse()
        }
    }

    override suspend fun setCourse(
        course: Course,
        checkPoints: List<CheckPoint>
    ) {
        courseRemoteDatasource.setCourse(course.toRemoteCourse())
        courseLocalDatasource.setCourse(course.toLocalCourse())
    }

    override suspend fun updateMetaCheckpoint(
        courseId: String,
        metaCheckPoint: MetaCheckPoint
    ): Boolean {
        return courseRemoteDatasource.updateMetaCheckpoint(
            courseId,
            metaCheckPoint.toDataMetaCheckPoint()
        ).apply {
            courseLocalDatasource.updateMetaCheckPoint(
                courseId,
                metaCheckPoint.toDataMetaCheckPoint()
            )
        }
    }
}

