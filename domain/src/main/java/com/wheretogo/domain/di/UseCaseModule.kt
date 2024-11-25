package com.wheretogo.domain.di

import com.wheretogo.domain.usecase.FetchJourneyWithoutPointsUseCase
import com.wheretogo.domain.usecase.GetJourneyUseCase
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import com.wheretogo.domain.usecase.UserProfileUpdateUseCase
import com.wheretogo.domain.usecase.UserSignInUseCase
import com.wheretogo.domain.usecase.UserSignOutUseCase
import com.wheretogo.domain.usecase.UserSignUpAndSignInUseCase
import com.wheretogo.domain.usecase.UserSignUpUseCase
import com.wheretogo.domain.usecaseimpl.FetchJourneyWithoutPointsUseCaseImpl
import com.wheretogo.domain.usecaseimpl.GetJourneyUseCaseImpl
import com.wheretogo.domain.usecaseimpl.GetNearByJourneyUseCaseImpl
import com.wheretogo.domain.usecaseimpl.UserProfileUpdateUseCaseImpl
import com.wheretogo.domain.usecaseimpl.UserSignInUseCaseImpl
import com.wheretogo.domain.usecaseimpl.UserSignOutUseCaseImpl
import com.wheretogo.domain.usecaseimpl.UserSignUpAndSignInUseCaseImpl
import com.wheretogo.domain.usecaseimpl.UserSignUpUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindGetJourneyUseCase(useCaseImpl: GetJourneyUseCaseImpl): GetJourneyUseCase

    @Binds
    abstract fun bindGetNearByJourneyUseCase(useCaseImpl: GetNearByJourneyUseCaseImpl): GetNearByJourneyUseCase

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
}

