package com.dhkim139.wheretogo.di

import com.dhkim139.wheretogo.mock.MockAddressRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockAppRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockAuthRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockCheckPointRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockCommentRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockCourseRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockImageRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockReportRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockRouteRemoteDatasourceImpl
import com.dhkim139.wheretogo.mock.MockUserRemoteDatasourceImpl
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
import com.wheretogo.data.datasourceimpl.AppLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.AppRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.CheckPointLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.CourseLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.GuestRemoteDatasourceImpl
import com.wheretogo.data.datasourceimpl.ImageLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.ReportLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.RouteLocalDatasourceImpl
import com.wheretogo.data.datasourceimpl.UserLocalDatasourceImpl
import com.wheretogo.data.di.DatasourceModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatasourceModule::class]
)
abstract class MockDatasourceModule {

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
    abstract fun bindAppRemoteDatasource(datasource: MockAppRemoteDatasourceImpl): AppRemoteDatasource

    @Binds
    abstract fun bindGuestRemoteDatasource(datasource: GuestRemoteDatasourceImpl): GuestRemoteDatasource


    @Binds
    abstract fun bindImageRemoteDatasource(datasource: MockImageRemoteDatasourceImpl): ImageRemoteDatasource

    @Binds
    abstract fun bindRouteRemoteDatasource(datasource: MockRouteRemoteDatasourceImpl): RouteRemoteDatasource

    @Binds
    abstract fun bindAddressRemoteDatasource(datasource: MockAddressRemoteDatasourceImpl): AddressRemoteDatasource

    @Binds
    abstract fun bindAuthRemoteDatasource(datasource: MockAuthRemoteDatasourceImpl): AuthRemoteDatasource

    @Binds
    abstract fun bindUserRemoteDatasource(datasource: MockUserRemoteDatasourceImpl): UserRemoteDatasource

    @Binds
    abstract fun bindCourseRemoteDatasource(datasource: MockCourseRemoteDatasourceImpl): CourseRemoteDatasource

    @Binds
    abstract fun bindCheckPointRemoteDatasource(datasource: MockCheckPointRemoteDatasourceImpl): CheckPointRemoteDatasource

    @Binds
    abstract fun bindCommentRemoteDatasource(datasource: MockCommentRemoteDatasourceImpl): CommentRemoteDatasource

    @Binds
    abstract fun bindReportRemoteDatasource(datasource: MockReportRemoteDatasourceImpl): ReportRemoteDatasource

}