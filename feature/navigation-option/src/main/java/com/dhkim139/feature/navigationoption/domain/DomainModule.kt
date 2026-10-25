package com.dhkim139.feature.navigationoption.domain

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DomainModule {

    @Binds
    abstract fun bindGetCourseParkingUseCase(impl: GetCourseParkingUseCaseImpl): GetCourseParkingUseCase

    @Binds
    abstract fun bindGetMapAppIdUseCase(impl: GetMapAppIdUseCaseImpl): GetMapAppIdUseCase

    @Binds
    abstract fun bindSaveMapAppIdUseCase(impl: SaveMapAppIdUseCaseImpl): SaveMapAppIdUseCase

}