package com.wheretogo.presentation.handler

import com.wheretogo.domain.handler.HomeEvent
import com.wheretogo.domain.handler.HomeHandler
import com.dhkim139.core.ui.event.AppEvent
import com.dhkim139.core.ui.screen.AppScreen
import com.wheretogo.presentation.R
import com.dhkim139.core.ui.event.EventBus
import com.dhkim139.core.ui.model.EventMsg

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

            HomeEvent.CHECKIN_NAVIGATE -> EventBus.send(
                AppEvent.Navigation(
                    AppScreen.Home,
                    AppScreen.Checkin,
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