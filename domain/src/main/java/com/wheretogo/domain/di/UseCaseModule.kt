package com.wheretogo.domain.di

import com.wheretogo.domain.usecase.app.AppCheckBySignatureUseCase
import com.wheretogo.domain.usecase.app.ObserveSettingsUseCase
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.GetCheckpointForMarkerUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.checkpoint.ReportCheckPointUseCase
import com.wheretogo.domain.usecase.comment.AddCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.GetCommentForCheckPointUseCase
import com.wheretogo.domain.usecase.comment.RemoveCommentToCheckPointUseCase
import com.wheretogo.domain.usecase.comment.ReportCommentUseCase
import com.wheretogo.domain.usecase.course.AddCourseUseCase
import com.wheretogo.domain.usecase.course.GetNearByCourseUseCase
import com.wheretogo.domain.usecase.course.RemoveCourseUseCase
import com.wheretogo.domain.usecase.course.ReportCourseUseCase
import com.wheretogo.domain.usecase.user.DeleteUserUseCase
import com.wheretogo.domain.usecase.user.GetUserProfileStreamUseCase
import com.wheretogo.domain.usecase.user.UserSignOutUseCase
import com.wheretogo.domain.usecase.user.UserSignUpAndSignInUseCase
import com.wheretogo.domain.usecase.util.CreateRouteUseCase
import com.wheretogo.domain.usecase.util.GetImageForPopupUseCase
import com.wheretogo.domain.usecase.util.GetLatLngFromAddressUseCase
import com.wheretogo.domain.usecase.util.SearchKeywordUseCase
import com.wheretogo.domain.usecase.util.UpdateLikeUseCase
import com.wheretogo.domain.usecaseimpl.app.AppCheckBySignatureUseCaseImpl
import com.wheretogo.domain.usecaseimpl.app.ObserveSettingsUseCaseImpl
import com.wheretogo.domain.usecaseimpl.checkpoint.AddCheckpointToCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.checkpoint.GetCheckpointForMarkerUseCaseImpl
import com.wheretogo.domain.usecaseimpl.checkpoint.RemoveCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.checkpoint.ReportCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.comment.AddCommentToCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.comment.GetCommentForCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.comment.RemoveCommentToCheckPointUseCaseImpl
import com.wheretogo.domain.usecaseimpl.comment.ReportCommentUseCaseImpl
import com.wheretogo.domain.usecaseimpl.course.AddCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.course.GetNearByCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.course.RemoveCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.course.ReportCourseUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.DeleteUserUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.GetUserProfileStreamUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignOutUseCaseImpl
import com.wheretogo.domain.usecaseimpl.user.UserSignUpAndSignInUseCaseImpl
import com.wheretogo.domain.usecaseimpl.util.CreateRouteUseCaseImpl
import com.wheretogo.domain.usecaseimpl.util.GetImageForPopupUseCaseImpl
import com.wheretogo.domain.usecaseimpl.util.GetLatLngFromAddressUseCaseImpl
import com.wheretogo.domain.usecaseimpl.util.SearchKeywordUseCaseImpl
import com.wheretogo.domain.usecaseimpl.util.UpdateLikeUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindAppCheckBySignatureUseCase(useCaseImpl: AppCheckBySignatureUseCaseImpl): AppCheckBySignatureUseCase

    @Binds
    abstract fun bindObserveSettingsUseCase(useCaseImpl: ObserveSettingsUseCaseImpl): ObserveSettingsUseCase


    @Binds
    abstract fun bindGetNearByJourneyUseCase(useCaseImpl: GetNearByCourseUseCaseImpl): GetNearByCourseUseCase

    @Binds
    abstract fun bindUserSignOutUseCase(useCaseImpl: UserSignOutUseCaseImpl): UserSignOutUseCase

    @Binds
    abstract fun bindUserSignUpAndSignInUseCase(useCaseImpl: UserSignUpAndSignInUseCaseImpl): UserSignUpAndSignInUseCase

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
    abstract fun bindGetImageForPopupUseCase(useCaseImpl: GetImageForPopupUseCaseImpl): GetImageForPopupUseCase

    @Binds
    abstract fun bindCreateRouteUseCase(useCaseImpl: CreateRouteUseCaseImpl): CreateRouteUseCase

    @Binds
    abstract fun bindAddCourseUseCase(useCaseImpl: AddCourseUseCaseImpl): AddCourseUseCase

    @Binds
    abstract fun bindAddCheckpointUseCase(useCaseImpl: AddCheckpointToCourseUseCaseImpl): AddCheckpointToCourseUseCase

    @Binds
    abstract fun bindGetUserProfileUseCase(useCaseImpl: GetUserProfileStreamUseCaseImpl): GetUserProfileStreamUseCase

    @Binds
    abstract fun bindDeleteUserUseCase(useCaseImpl: DeleteUserUseCaseImpl): DeleteUserUseCase

    @Binds
    abstract fun bindUpdateLikeUseCase(useCaseImpl: UpdateLikeUseCaseImpl): UpdateLikeUseCase

    @Binds
    abstract fun bindSearchAddressUseCase(useCaseImpl: SearchKeywordUseCaseImpl): SearchKeywordUseCase

    @Binds
    abstract fun bindGetLatLngFromAddressUseCase(useCaseImpl: GetLatLngFromAddressUseCaseImpl): GetLatLngFromAddressUseCase
}

