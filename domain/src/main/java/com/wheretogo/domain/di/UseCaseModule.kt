package com.wheretogo.domain.di

import com.wheretogo.domain.usecase.map.FetchJourneyWithoutPointsUseCase
import com.wheretogo.domain.usecase.map.GetCheckPointByCourseUseCase
import com.wheretogo.domain.usecase.map.GetCommentByCheckPointUseCase
import com.wheretogo.domain.usecase.map.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.map.GetImageByCheckpointUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.user.RemoveHistoryUseCase
import com.wheretogo.domain.usecase.user.UpdateHistoryUseCase
import com.wheretogo.domain.usecase.user.UserProfileUpdateUseCase
import com.wheretogo.domain.usecase.user.UserSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignUpUseCase
import com.wheretogo.domain.usecaseimpl.FetchJourneyWithoutPointsUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetCheckPointByCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetCommentByCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetHistoryStreamUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetImageByCheckpointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetNearByCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.RemoveHistoryUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UpdateHistoryUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserProfileUpdateUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignInUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignOutUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignUpAndSignInUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignUpUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindGetNearByJourneyUseCase(useCaseImpl: GetNearByCourseUseCaseImpl): GetNearByCourseUseCase

    @Binds
    abstract fun bindFetchCourseUseCase(useCaseImpl: FetchJourneyWithoutPointsUseCaseImpl): FetchJourneyWithoutPointsUseCase

    @Binds
    abstract fun bindUserSignInUseCase(useCaseImpl: UserSignInUseCaseImpl): UserSignInUseCase

    @Binds
    abstract fun bindUserSignUpUseCase(useCaseImpl: UserSignUpUseCaseImpl): UserSignUpUseCase

    @Binds
    abstract fun bindUserSignOutUseCase(useCaseImpl: UserSignOutUseCaseImpl): UserSignOutUseCase

    @Binds
    abstract fun bindUserSignUpAndSignInUseCase(useCaseImpl: UserSignUpAndSignInUseCaseImpl): UserSignUpAndSignInUseCase

    @Binds
    abstract fun bindUserProfileUpdateUseCase(useCaseImpl: UserProfileUpdateUseCaseImpl): UserProfileUpdateUseCase


    @Binds
    abstract fun bindGetCheckPointByCourseUseCase(useCaseImpl: GetCheckPointByCourseUseCaseImpl): GetCheckPointByCourseUseCase

    @Binds
    abstract fun bindGetCommentByCheckPointUseCase(useCaseImpl: GetCommentByCheckPointUseCaseImpl): GetCommentByCheckPointUseCase

    @Binds
    abstract fun bindGetHistoryStreamUseCase(useCaseImpl: GetHistoryStreamUseCaseImpl): GetHistoryStreamUseCase

    @Binds
    abstract fun bindGetImageByCheckpointUseCase(useCaseImpl: GetImageByCheckpointUseCaseImpl): GetImageByCheckpointUseCase

    @Binds
    abstract fun bindUpdateHistoryUseCase(useCaseImpl: UpdateHistoryUseCaseImpl): UpdateHistoryUseCase

    @Binds
    abstract fun bindRemoveHistoryUseCase(useCaseImpl: RemoveHistoryUseCaseImpl): RemoveHistoryUseCase
}

