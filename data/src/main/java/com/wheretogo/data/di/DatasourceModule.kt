package com.wheretogo.data.di

import com.wheretogo.data.datasource.AddressRemoteDatasource
import com.wheretogo.data.datasource.AppLocalDatasource
import com.wheretogo.data.datasource.AppRemoteDatasource
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CheckPointRemoteDatasource
import com.wheretogo.data.datasource.CommentRemoteDatasource
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.CourseRemoteDatasource
import com.wheretogo.data.datasource.GuestRemoteDatasource
import com.wheretogo.data.datasource.ImageLocalDatasource
import com.wheretogo.data.datasource.ImageRemoteDatasource
import com.wheretogo.data.datasource.ReportLocalDatasource
import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.datasource.RouteLocalDatasource
import com.wheretogo.data.datasource.RouteRemoteDatasource
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.data.datasource.UserRemoteDatasource
import com.wheretogo.data.datasourceimpl.AddressRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.AppLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.AppRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.AuthRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CommentRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.GuestRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.ImageLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.ImageRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.ReportLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.ReportRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.UserLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.UserRemoteDatasourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


// 수정시 테스트를 위해 MockDatasourceModule 과 맞춤
@Module
@InstallIn(SingletonComponent::class)
abstract class DatasourceModule {

    //local
    @Binds
    abstract fun bindAppLocalDatasource(datasource: AppLocalDatasourceImpl): AppLocalDatasource

    @Binds
    abstract fun bindUserLocalDatasource(datasource: UserLocalDatasourceImpl): UserLocalDatasource

    @Binds
    abstract fun bindCourseLocalDatasource(datasource: CourseLocalDatasourceImpl): CourseLocalDatasource

    @Binds
    abstract fun bindCheckPointLocalDatasource(datasource: CheckPointLocalDatasourceImpl): CheckPointLocalDatasource

    @Binds
    abstract fun bindImageLocalDatasource(datasource: ImageLocalDatasourceImpl): ImageLocalDatasource

    @Binds
    abstract fun bindReportLocalDatasource(datasource: ReportLocalDatasourceImpl): ReportLocalDatasource

    @Binds
    abstract fun bindRouteLocalDatasource(datasource: RouteLocalDatasourceImpl): RouteLocalDatasource

    //remote
    @Binds
    abstract fun bindAppRemoteDatasource(datasource: AppRemoteDatasourceImpl): AppRemoteDatasource

    @Binds
    abstract fun bindGuestRemoteDatasource(datasource: GuestRemoteDatasourceImpl): GuestRemoteDatasource

    @Binds
    abstract fun bindImageRemoteDatasource(datasource: ImageRemoteDatasourceImpl): ImageRemoteDatasource

    @Binds
    abstract fun bindRouteRemoteDatasource(datasource: RouteRemoteDatasourceImpl): RouteRemoteDatasource

    @Binds
    abstract fun bindAddressRemoteDatasource(datasource: AddressRemoteDatasourceImpl): AddressRemoteDatasource

    @Binds
    abstract fun bindAuthRemoteDatasource(datasource: AuthRemoteDatasourceImpl): AuthRemoteDatasource

    @Binds
    abstract fun bindUserRemoteDatasource(datasource: UserRemoteDatasourceImpl): UserRemoteDatasource

    @Binds
    abstract fun bindCourseRemoteDatasource(datasource: CourseRemoteDatasourceImpl): CourseRemoteDatasource

    @Binds
    abstract fun bindCheckPointRemoteDatasource(datasource: CheckPointRemoteDatasourceImpl): CheckPointRemoteDatasource

    @Binds
    abstract fun bindCommentRemoteDatasource(datasource: CommentRemoteDatasourceImpl): CommentRemoteDatasource

    @Binds
    abstract fun bindReportRemoteDatasource(datasource: ReportRemoteDatasourceImpl): ReportRemoteDatasource
}