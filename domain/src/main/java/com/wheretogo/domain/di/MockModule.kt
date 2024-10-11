package com.wheretogo.domain.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


/*
* 실제 구현체는 git에 업로드 되지 않음
* todo 빌드오류시 주석을 해제해 사용
* */
@Module
@InstallIn(SingletonComponent::class)
abstract class MockModule {

  /*  @Binds
    abstract fun bindGetNearByJourneyUseCase(useCaseImpl: MockGetNearByJourneyUseCaseImpl): GetNearByJourneyUseCase

*/
}