package com.wheretogo.domain.di

import com.wheretogo.domain.usecase.community.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.community.GetImageInfoUseCase
import com.wheretogo.domain.usecase.community.GetReportUseCase
import com.wheretogo.domain.usecase.community.ModifyLikeUseCase
import com.wheretogo.domain.usecase.community.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.community.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.community.RemoveCourseUseCase
import com.wheretogo.domain.usecase.community.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.community.ReportCommentUseCase
import com.wheretogo.domain.usecase.community.ReportCourseUseCase
import com.wheretogo.domain.usecase.map.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.map.AddCourseUseCase
import com.wheretogo.domain.usecase.map.CreateRouteUseCase
import com.wheretogo.domain.usecase.map.FetchJourneyWithoutPointsUseCase
import com.wheretogo.domain.usecase.map.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.map.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.map.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.user.DeleteUserUseCase
import com.wheretogo.domain.usecase.user.GetHistoryStreamUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.RemoveHistoryUseCase
import com.wheretogo.domain.usecase.user.UpdateHistoryUseCase
import com.wheretogo.domain.usecase.user.UserProfileUpdateUseCase
import com.wheretogo.domain.usecase.user.UserSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.domain.usecase.user.UserSignUpUseCase
import com.wheretogo.domain.usecaseimpl.FetchJourneyWithoutPointsUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.AddCommentToCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.GetCommentForCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.GetImageInfoUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.GetReportUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.ModifyLikeUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.RemoveCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.RemoveCommentToCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.RemoveCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.ReportCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.ReportCommentUseCaseImpl
import com.wheretogo.domain.usecaseimpl.community.ReportCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.AddCheckpointToCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.AddCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.CreateRouteUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetCheckpointForMarkerUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetImageForPopupUseCaseImpl
import com.wheretogo.domain.usecaseimpl.map.GetNearByCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.DeleteUserUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.GetHistoryStreamUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.GetUserProfileStreamUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.RemoveHistoryUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UpdateHistoryUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserProfileUpdateUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignInUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignOutUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignUpAndSignInUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignUpUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindGetNearByJourneyUseCase(useCaseImpl: GetNearByCourseUseCaseImpl): GetNearByCourseUseCase

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


    @Binds
    abstract fun bindGetCheckPointForMarkerUseCase(useCaseImpl: GetCheckpointForMarkerUseCaseImpl): GetCheckpointForMarkerUseCase

    @Binds
    abstract fun bindGetCommentByCheckPointUseCase(useCaseImpl: GetCommentForCheckPointUseCaseImpl): GetCommentForCheckPointUseCase

    @Binds
    abstract fun bindAddCommentByCheckPointUseCase(useCaseImpl: AddCommentToCheckPointUseCaseImpl): AddCommentToCheckPointUseCase

    @Binds
    abstract fun bindRemoveCommentToChaeckPointUseCase(useCaseImpl: RemoveCommentToCheckPointUseCaseImpl): RemoveCommentToCheckPointUseCase

    @Binds
    abstract fun bindRemoveCourseUseCase(useCaseImpl: RemoveCourseUseCaseImpl): RemoveCourseUseCase

    @Binds
    abstract fun bindRemoveCheckPointUseCase(useCaseImpl: RemoveCheckPointUseCaseImpl): RemoveCheckPointUseCase

    @Binds
    abstract fun bindReportCommentUseCase(useCaseImpl: ReportCommentUseCaseImpl): ReportCommentUseCase

    @Binds
    abstract fun bindReportCourseUseCase(useCaseImpl: ReportCourseUseCaseImpl): ReportCourseUseCase

    @Binds
    abstract fun bindReportCheckPointUseCase(useCaseImpl: ReportCheckPointUseCaseImpl): ReportCheckPointUseCase

    @Binds
    abstract fun bindGetHistoryStreamUseCase(useCaseImpl: GetHistoryStreamUseCaseImpl): GetHistoryStreamUseCase

    @Binds
    abstract fun bindGetImageForPopupUseCase(useCaseImpl: GetImageForPopupUseCaseImpl): GetImageForPopupUseCase

    @Binds
    abstract fun bindUpdateHistoryUseCase(useCaseImpl: UpdateHistoryUseCaseImpl): UpdateHistoryUseCase

    @Binds
    abstract fun bindRemoveHistoryUseCase(useCaseImpl: RemoveHistoryUseCaseImpl): RemoveHistoryUseCase

    @Binds
    abstract fun bindCreateRouteUseCase(useCaseImpl: CreateRouteUseCaseImpl): CreateRouteUseCase

    @Binds
    abstract fun bindAddCourseUseCase(useCaseImpl: AddCourseUseCaseImpl): AddCourseUseCase

    @Binds
    abstract fun bindAddCheckpointUseCase(useCaseImpl: AddCheckpointToCourseUseCaseImpl): AddCheckpointToCourseUseCase

    @Binds
    abstract fun bindGetImageInfoUseCase(useCaseImpl: GetImageInfoUseCaseImpl): GetImageInfoUseCase

    @Binds
    abstract fun bindGetUserProfileUseCase(useCaseImpl: GetUserProfileStreamUseCaseImpl): GetUserProfileStreamUseCase

    @Binds
    abstract fun bindDeleteUserUseCase(useCaseImpl: DeleteUserUseCaseImpl): DeleteUserUseCase

    @Binds
    abstract fun bindModifyLikeUseCase(useCaseImpl: ModifyLikeUseCaseImpl): ModifyLikeUseCase

    @Binds
    abstract fun bindGetReportUseCase(useCaseImpl: GetReportUseCaseImpl): GetReportUseCase
}

