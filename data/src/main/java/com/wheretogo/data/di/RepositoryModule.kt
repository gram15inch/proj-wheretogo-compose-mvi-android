package com.wheretogo.data.di

import com.wheretogo.data.repositoryimpl.AuthRepositoryImpl
import com.wheretogo.data.repositoryimpl.CheckPointRepositoryImpl
import com.wheretogo.data.repositoryimpl.CommentRepositoryImpl
import com.wheretogo.data.repositoryimpl.CourseRepositoryImpl
import com.wheretogo.data.repositoryimpl.ImageRepositoryImpl
import com.wheretogo.data.repositoryimpl.UserRepositoryImpl
import com.wheretogo.domain.repository.AuthRepository
import com.wheretogo.domain.repository.CheckPointRepository
import com.wheretogo.domain.repository.CommentRepository
import com.wheretogo.domain.repository.CourseRepository
import com.wheretogo.domain.repository.ImageRepository
import com.wheretogo.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCourseRepository(repositoryImpl: CourseRepositoryImpl): CourseRepository

    @Binds
    abstract fun bindUserRepository(repositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindAuthRepository(repositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindCheckPointRepository(repositoryImpl: CheckPointRepositoryImpl): CheckPointRepository

    @Binds
    abstract fun bindImageRepository(repositoryImpl: ImageRepositoryImpl): ImageRepository

    @Binds
    abstract fun bindCommentRepository(repositoryImpl: CommentRepositoryImpl): CommentRepository

}