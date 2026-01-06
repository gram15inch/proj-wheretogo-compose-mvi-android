package com.wheretogo.presentation.handler

import com.wheretogo.domain.handler.HomeEvent
import com.wheretogo.domain.handler.HomeHandler
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppScreen
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg

class HomeHandlerImpl() : HomeHandler {
    override suspend fun handle(event: HomeEvent) {
        when (event) {
            HomeEvent.DRIVE_NAVIGATE -> EventBus.send(
                AppEvent.Navigation(
                    AppScreen.Home,
                    AppScreen.Drive,
                    false
                )
            )

            HomeEvent.COURSE_ADD_NAVIGATE -> EventBus.send(
                AppEvent.Navigation(
                    AppScreen.Home,
                    AppScreen.CourseAdd,
                    false
                )
            )

            HomeEvent.GUIDE_START -> EventBus.send(AppEvent.SnackBar(EventMsg(R.string.tutorial_start)))
            HomeEvent.GUIDE_STOP -> EventBus.send(AppEvent.SnackBar(EventMsg(R.string.tutorial_stop)))
        }
    }

    override suspend fun handle(error: Throwable): Throwable {
        return error
    }
}