package com.wheretogo.data.di

import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.datasource.LikeRemoteDatasource
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.LikeRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.UserLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.UserRemoteDatasourceImpl
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

    @Binds
    abstract fun bindCourseLocalDatasource(datasource: CourseLocalDatasourceImpl): CourseLocalDatasource

    @Binds
    abstract fun bindCourseRemoteDatasource(datasource: CourseRemoteDatasourceImpl): CourseRemoteDatasource

    @Binds
    abstract fun bindCheckPointLocalDatasource(datasource: CheckPointLocalDatasourceImpl): CheckPointLocalDatasource

    @Binds
    abstract fun bindCheckPointRemoteDatasource(datasource: CheckPointRemoteDatasourceImpl): CheckPointRemoteDatasource

    @Binds
    abstract fun bindRouteRemoteDatasource(datasource: RouteRemoteDatasourceImpl): RouteRemoteDatasource

    @Binds
    abstract fun bindLikeRemoteDatasource(datasource: LikeRemoteDatasourceImpl): LikeRemoteDatasource


}