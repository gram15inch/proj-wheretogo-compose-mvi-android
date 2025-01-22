package com.dhkim139.wheretogo.di

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.model.course.DataMetaCheckPoint
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.HistoryType
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
import com.wheretogo.domain.model.user.ProfilePublic
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MockModelModule {
    private val courseGroup = getCourseDummy()

    @Provides
    @Singleton
    fun provideRemoteUser(): MockRemoteUser {
        return MockRemoteUser(
            token = "token1",
            profile = Profile(
                uid = "uid1",
                public = ProfilePublic(
                    name = "user1"
                ),
                private = ProfilePrivate(
                    mail = "user1@gmail.com",
                    authCompany = AuthCompany.GOOGLE.name
                )
            ),
            history = mapOf(
                HistoryType.COMMENT to listOf(""),
                HistoryType.COURSE to courseGroup.map { it.courseId },
                HistoryType.CHECKPOINT to courseGroup.flatMap { it.checkpointIdGroup },
                HistoryType.LIKE to listOf(""),
                HistoryType.BOOKMARK to listOf(""),
                HistoryType.REPORT to listOf(""),
            )
        )
    }

    @Provides
    @Singleton
    fun provideRemoteCourseGroup(): List<RemoteCourse> {
        return courseGroup.map { it.toRemoteCourse(checkPoint = DataMetaCheckPoint(it.checkpointIdGroup)) }
    }
}