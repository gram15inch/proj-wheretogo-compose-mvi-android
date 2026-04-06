package com.wheretogo.data.repositoryimpl

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.CoursePolicy
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.di.ClearCache
import com.wheretogo.data.di.CourseCache
import com.wheretogo.data.feature.mapDataError
import com.wheretogo.data.feature.mapDomainError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.data.toCourse
import com.wheretogo.data.toLocalCourse
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseAddRequest
import com.wheretogo.domain.repository.CourseRepository
import de.huxhorn.sulky.ulid.ULID
import timber.log.Timber
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseRemoteDatasource: CourseRemoteDatasource,
    private val courseLocalDatasource: CourseLocalDatasource,
    @CourseCache private val coursePolicy: CachePolicy,
    @ClearCache private val clearPolicy: CachePolicy
) : CourseRepository {
    private val cacheCourseGroupByKeyword = mutableMapOf<String, List<Course>>()

    override suspend fun getCourse(courseId: String): Result<Course> {
        return courseLocalDatasource.getCourse(courseId)
            .mapSuccess { old ->
                if (old != null)
                    return@mapSuccess Result.success(old)

                courseRemoteDatasource.getCourse(courseId)
                    .mapSuccess { courseLocalDatasource.setCourse(listOf(it.toLocalCourse())) }
                    .mapSuccess { courseLocalDatasource.getCourse(courseId) }
            }.mapDataError().mapCatching { it.toCourse() }.mapDomainError()
    }

    override suspend fun getCourseGroupByGeoHash(geoHash: String): Result<List<Course>> {
        return courseLocalDatasource.getLatestUpdate().mapSuccess { old ->
            val now = System.currentTimeMillis()
            val num = (now - old).toFloat() / (1000 * 60 * if(old == 0L) CoursePolicy.minuteWhenEmpty else CoursePolicy.minuteWhenNotEmpty)
            val formatStr = String.format("%.1f%%", num * 100)
            Timber.d("course expire: $formatStr")

            // 전체 코스 업데이트 확인
            val isExpire = coursePolicy.isExpired(old, old==0L)

            if (isExpire) {
                courseLocalDatasource.setLatestUpdate(now)
                // 변경 or 추가된 코스 가져오기
                courseRemoteDatasource.getCourseGroupByUpdateAt(old).mapSuccess { remote->
                    remote.map { it.toLocalCourse() }.run {
                        courseLocalDatasource.setCourse(this)
                        Result.success(this)
                    }
                }
            } else {
                courseLocalDatasource.getCourseGroupByGeoHash(geoHash)
            }.map { local-> local.map { it.toCourse() }}
        }.mapDomainError()
    }

    override suspend fun getCourseGroupByKeyword(keyword: String): Result<List<Course>> {
        return runCatching { cacheCourseGroupByKeyword.getOrDefault(keyword, null) }
            .mapSuccess {
                if (it != null)
                    return@mapSuccess Result.success(it)

                courseRemoteDatasource.getCourseGroupByKeyword(keyword)
                    .mapCatching { it.map { it.toCourse() } }
                    .onSuccess { cacheCourseGroupByKeyword.put(keyword, it) }
            }.mapDataError().mapDomainError()
    }

    override suspend fun addCourse(
        request: CourseAddRequest
    ): Result<Course> {
        val courseId = "CS${ULID().nextULID()}"
        val remote = request.toCourse(courseId)
        val local = remote.toLocalCourse()
        return courseRemoteDatasource.setCourse(remote)
            .mapSuccess {
                courseLocalDatasource.setCourse(listOf(local))
            }.mapSuccess {
                courseLocalDatasource.getCourse(courseId)
            }.mapDataError().mapCatching { it.toCourse() }.mapDomainError()
    }

    override suspend fun removeCourse(courseId: String): Result<Unit> {
        return courseRemoteDatasource.removeCourse(courseId).mapSuccess {
            courseLocalDatasource.removeCourse(courseId)
        }.mapDataError().mapDomainError()
    }


    override suspend fun clearExpired(): Result<Int> {
        var removed = 0

        return courseLocalDatasource.getCourseByIsHide(true).mapCatching {
            it.filter { course->
                clearPolicy.isExpired( course.updateAt ,false)
            }
        }.mapSuccess { filtered->
            runCatching {
                filtered.forEach {
                    courseLocalDatasource.removeCourse(it.courseId)
                    removed++
                }
            }
        }.mapCatching { removed }
    }

    override suspend fun clearCache(): Result<Unit> {
        return courseLocalDatasource.clear().mapDataError().mapDomainError()
    }
}

