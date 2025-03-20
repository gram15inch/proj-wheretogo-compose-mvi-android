package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import com.wheretogo.data.toCourse
import com.wheretogo.data.toDataMetaCheckPoint
import com.wheretogo.data.toLocalCourse
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.MetaCheckPoint
import com.wheretogo.domain.repository.CourseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseRemoteDatasource: CourseRemoteDatasource,
    private val courseLocalDatasource: CourseLocalDatasource,
) : CourseRepository {

    override suspend fun getCourse(courseId: String): Result<Course> {
        return runCatching {
            courseLocalDatasource.getCourse(courseId)?.toCourse() ?: run {
                courseRemoteDatasource.getCourse(courseId)?.run {
                    this.toLocalCourse()
                }?.toCourse() ?: throw NoSuchElementException("Course not found with id: $courseId")
            }
        }
    }

    override suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<Course>> {
        return runCatching {
            if (courseLocalDatasource.isExistMetaGeoHash(geoHash)) { // geohash로 구역 호출 여부 확인
                courseLocalDatasource.getCourseGroupByGeoHash(geoHash)
            } else {
                courseRemoteDatasource.getCourseGroupByGeoHash(geoHash, "$geoHash\uf8ff") // 구역에 해당하는 코스들 가져오기
                    .run {
                        coroutineScope {
                            map {
                                async {
                                    it.toLocalCourse()  //경로(points) 없이 변환
                                        .apply {
                                            courseLocalDatasource.setMetaGeoHash( //체크포인트 아이디만 저장
                                                LocalMetaGeoHash(geoHash, System.currentTimeMillis())
                                            )
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
    }

    override suspend fun setCourse(
        course: Course,
        checkPoints: List<CheckPoint>
    ): Result<Unit> {
        return runCatching {
            courseRemoteDatasource.setCourse(course.toRemoteCourse())
            courseLocalDatasource.setCourse(course.toLocalCourse())
        }
    }

    override suspend fun removeCourse(courseId: String): Result<Unit> {
        return runCatching {
            courseRemoteDatasource.removeCourse(courseId)
            courseLocalDatasource.removeCourse(courseId)
        }
    }

    override suspend fun updateMetaCheckpoint(
        courseId: String,
        metaCheckPoint: MetaCheckPoint
    ): Result<Unit> {
        return runCatching {
            courseRemoteDatasource.updateMetaCheckpoint(
                courseId,
                metaCheckPoint.toDataMetaCheckPoint()
            )
            courseLocalDatasource.updateMetaCheckPoint(
                courseId,
                metaCheckPoint.toDataMetaCheckPoint()
            )
        }
    }
}

