package com.wheretogo.presentation.handler

import com.wheretogo.domain.handler.DriveEvent
import com.wheretogo.domain.handler.DriveHandler
import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.toAppError

class DriveHandlerImpl(val errorHandler: ErrorHandler) : DriveHandler {
    override suspend fun handle(event: DriveEvent) {
        when (event) {
            DriveEvent.ADD_DONE -> EventBus.send(AppEvent.SnackBar(EventMsg(R.string.add_done)))
            DriveEvent.REMOVE_DONE -> EventBus.send(AppEvent.SnackBar(EventMsg(R.string.remove_done)))
            DriveEvent.REPORT_DONE -> EventBus.send(AppEvent.SnackBar(EventMsg(R.string.report_done)))
        }
    }

    override suspend fun handle(error: Throwable): Throwable {
        val appError = error.toAppError()
        when (appError) {
            is AppError.MapNotSupportExcludeLocation ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.no_supprot_app_exclude_my_loction)))

            else -> return errorHandler.handle(error)
        }
        return appError
    }
}