package com.wheretogo.domain.di

import com.wheretogo.domain.mock.MockGetJourneyUseCaseImpl
import com.wheretogo.domain.mock.MockGetNearByJourneyUseCaseImpl
import com.wheretogo.domain.usecase.GetJourneyUseCase
import com.wheretogo.domain.usecase.GetNearByJourneyUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent



 //실제 구현체는 git에 업로드 되지 않음

@Module
@InstallIn(SingletonComponent::class)
abstract class MockModule {
    @Binds
    abstract fun bindGetJourneyUseCase(useCaseImpl: MockGetJourneyUseCaseImpl): GetJourneyUseCase

    @Binds
    abstract fun bindGetNearByJourneyUseCase(useCaseImpl: MockGetNearByJourneyUseCaseImpl): GetNearByJourneyUseCase
}
