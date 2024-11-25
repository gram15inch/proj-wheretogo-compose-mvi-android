package com.wheretogo.data.di

import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserLocalDatasourceImpl
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.datasource.UserRemoteDatasourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class DatasourceModule {

    @Binds
    abstract fun bindUserLocalDatasource(datasource: UserLocalDatasourceImpl): UserLocalDatasource

    @Binds
    abstract fun bindUserRemoteDatasource(datasource: UserRemoteDatasourceImpl): UserRemoteDatasource

}