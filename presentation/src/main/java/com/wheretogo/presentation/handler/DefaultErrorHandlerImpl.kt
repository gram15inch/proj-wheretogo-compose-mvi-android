package com.wheretogo.presentation.handler

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.toAppError
import timber.log.Timber

class DefaultErrorHandlerImpl : ErrorHandler {
    override suspend fun handle(error: Throwable): Throwable {
        Timber.tag("policy_").d("Throwable -> AppError: ${error.stackTraceToString()}")
        val appError = error.toAppError()
        when (appError) {
            is AppError.NeedSignIn -> {
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.need_login)))
                EventBus.send(AppEvent.SignInScreen)
            }

            is AppError.NetworkError ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.network_error)))

            is AppError.WaitCoolDown ->
                EventBus.send(
                    AppEvent.SnackBar(
                        EventMsg(
                            R.string.cool_down_error,
                            arg = "${appError.remainTimeInMinute}"
                        )
                    )
                )

            is AppError.Ignore -> {}

            else -> {
                Timber.e(error.stackTraceToString())
                FirebaseCrashlytics.getInstance().recordException(error)
            }
        }
        return error
    }
}