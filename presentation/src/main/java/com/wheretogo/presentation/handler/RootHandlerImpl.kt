package com.wheretogo.presentation.handler

import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.domain.handler.RootEvent
import com.wheretogo.domain.handler.RootHandler
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg

class RootHandlerImpl(val errorHandler: ErrorHandler) : RootHandler {
    override suspend fun handle(event: RootEvent) {
        when (event) {
            RootEvent.APP_CHECK_SUCCESS -> EventBus.send(AppEvent.SnackBar(EventMsg(R.string.app_check_success)))
        }
    }

    override suspend fun handle(error: Throwable): Throwable {
        return errorHandler.handle(error)
    }
}