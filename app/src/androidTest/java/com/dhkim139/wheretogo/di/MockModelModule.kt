package com.dhkim139.wheretogo.di

import com.dhkim139.wheretogo.mock.model.MockRemoteUser
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.toLocalHistoryIdGroup
import com.wheretogo.data.toRemoteCourse
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.feature.hashSha256
import com.wheretogo.domain.model.dummy.getCourseDummy
import com.wheretogo.domain.model.history.HistoryIdGroup
import com.wheretogo.domain.model.user.History
import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.user.ProfilePrivate
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
        val remoteCourseGroup =
            courseGroup.associateBy({it.courseId},{listOf(it.courseId)})
        val remoteCheckpointGroup =
            courseGroup.associateBy({it.courseId},{it.checkpointIdGroup})

        return MockRemoteUser(
            token = "token1",
            profile = Profile(
                uid = "mockUid1",
                name = "mockUser1",
                hashMail = hashSha256("user1@gmail.com"),
                private = ProfilePrivate(
                    mail = "user1@gmail.com",
                    authCompany = AuthCompany.GOOGLE.name
                )
            ),
            history = History().run {
                copy(
                    course = course.copy(
                        historyIdGroup = HistoryIdGroup(remoteCourseGroup.toLocalHistoryIdGroup().groupById)
                    ),
                    checkpoint = checkpoint.copy(
                        historyIdGroup = HistoryIdGroup(remoteCheckpointGroup.toLocalHistoryIdGroup().groupById)
                    )
                )
            }
        )
    }

    @Provides
    @Singleton
    fun provideRemoteCourseGroup(): List<RemoteCourse> {
        return courseGroup.map { it.toRemoteCourse() }
    }
}