package com.wheretogo.domain.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


/*
* 실제 구현체는 git에 업로드 되지 않음
* todo usecaseimpl 없을때 주석을 해제해 사용
* */
@Module
@InstallIn(SingletonComponent::class)
abstract class MockModule {

  /*
    @Binds
    abstract fun bindGetJourneyUseCase(useCaseImpl: GetJourneyUseCaseImpl): GetJourneyUseCase

    @Binds
    abstract fun bindGetNearByJourneyUseCase(useCaseImpl: MockGetNearByJourneyUseCaseImpl): GetNearByJourneyUseCase
*/
}