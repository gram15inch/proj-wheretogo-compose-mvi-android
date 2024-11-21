package com.wheretogo.data.di

import com.wheretogo.data.repository.AuthRepositoryImpl
import com.wheretogo.data.repository.CourseRepositoryImpl
import com.wheretogo.data.repository.JourneyRepositoryImpl
import com.wheretogo.data.repository.UserRepositoryImpl
import com.wheretogo.domain.repository.AuthRepository
import com.wheretogo.domain.repository.CourseRepository
import com.wheretogo.domain.repository.JourneyRepository
import com.wheretogo.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindJourneyRepository(repositoryImpl: JourneyRepositoryImpl): JourneyRepository

    @Binds
    abstract fun bindCourseRepository(repositoryImpl: CourseRepositoryImpl): CourseRepository

    @Binds
    abstract fun bindUserRepository(repositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindAuthRepository(repositoryImpl: AuthRepositoryImpl): AuthRepository

}