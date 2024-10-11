package com.wheretogo.domain.di

import com.wheretogo.domain.usecase.GetJourneyUseCase
import com.wheretogo.domain.usecaseimpl.GetJourneyUseCaseImpl
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import com.wheretogo.domain.usecaseimpl.GetNearByJourneyUseCaseImpl
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

}