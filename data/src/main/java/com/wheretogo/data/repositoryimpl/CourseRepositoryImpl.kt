package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.di.CourseCache
import com.wheretogo.data.model.meta.LocalMetaGeoHash
import com.wheretogo.data.toCourse
import com.wheretogo.data.toLocalCourse
import com.wheretogo.data.toLocalSnapshot
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.data.toSnapshot
import com.wheretogo.domain.model.map.Course
import com.wheretogo.domain.model.map.Snapshot
import com.wheretogo.domain.repository.CourseRepository
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseRemoteDatasource: CourseRemoteDatasource,
    private val courseLocalDatasource: CourseLocalDatasource,
    @CourseCache private val cachePolicy: CachePolicy
) : CourseRepository {
    private val cacheCourseGroupByKeyword = mutableMapOf<String, List<Course>>()
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
                val newCourseGroup= courseRemoteDatasource.getCourseGroupByGeoHash(geoHash, "$geoHash\uf8ff") // 구역에 해당하는 코스들 가져오기
                    .map { it.toLocalCourse() }
                courseLocalDatasource.setCourse(newCourseGroup) // 불러온 코스 저장
                courseLocalDatasource.setMetaGeoHash(LocalMetaGeoHash(geoHash, System.currentTimeMillis()))
                newCourseGroup
            }.map { it.toCourse() }
        }
    }

    override suspend fun getCourseGroupByKeyword(keyword: String): Result<List<Course>> {
        return runCatching {
            cacheCourseGroupByKeyword.getOrPut(keyword) {
                courseRemoteDatasource.getCourseGroupByKeyword(keyword).map { it.toCourse() }
            }
        }
    }

    override suspend fun setCourse(
        course: Course,
        keyword: List<String>
    ): Result<Unit> {
        return runCatching {
            courseRemoteDatasource.setCourse(course.toRemoteCourse(keyword = keyword))
            courseLocalDatasource.setCourse(listOf(course.toLocalCourse()))
        }
    }

    override suspend fun removeCourse(courseId: String): Result<Unit> {
        return runCatching {
            courseRemoteDatasource.removeCourse(courseId)
            courseLocalDatasource.removeCourse(courseId)
        }
    }

    override suspend fun getSnapshot(courseId: String): Snapshot {
        return courseLocalDatasource.getCourse(courseId)?.checkpointSnapshot
            ?.copy(refId = courseId)?.toSnapshot() ?: Snapshot(refId = courseId)
    }

    override suspend fun updateSnapshot(snapshot: Snapshot): Result<Unit> {
        return runCatching {
            if (cachePolicy.isExpired(
                    timestamp = snapshot.timeStamp,
                    isEmpty = snapshot.indexIdGroup.isEmpty()
                )
            ) {
                courseLocalDatasource.updateSnapshot(snapshot.toLocalSnapshot())
            }
        }
    }

    override suspend fun appendIndex(snapshot: Snapshot): Result<Unit> {
        return runCatching {
            courseLocalDatasource.appendIndex(
                localSnapshot = snapshot.toLocalSnapshot()
            )
        }
    }

    override suspend fun removeIndex(snapshot: Snapshot): Result<Unit> {
        return runCatching {
            courseLocalDatasource.removeIndex(
                localSnapshot = snapshot.toLocalSnapshot()
            )
        }
    }
}

