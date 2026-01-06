package com.wheretogo.presentation.handler

import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.domain.handler.LoginEvent
import com.wheretogo.domain.handler.LoginHandler
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.toAppError

class LoginHandlerImpl(val errorHandler: ErrorHandler) : LoginHandler {
    override suspend fun handle(event: LoginEvent, arg: String) {
        when (event) {
            LoginEvent.SIGN_IN_SUCCESS -> {
                EventBus.result(AppEvent.SignInScreen, true)
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.welcome_user, arg)))
            }

            LoginEvent.SIGN_IN_FAIL -> EventBus.result(AppEvent.SignInScreen, false)
        }
    }

    override suspend fun handle(error: Throwable): Throwable {
        val appError = error.toAppError()
        when (appError) {
            is AppError.CredentialError ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.google_auth)))

            is AppError.Unavailable ->
                EventBus.send(
                    AppEvent.SnackBar(
                        EventMsg(
                            R.string.unavailable_user,
                            arg = appError.msg
                        )
                    )
                )

            else -> return errorHandler.handle(error)
        }
        return appError
    }
}