package com.wheretogo.presentation.handler

import com.wheretogo.domain.DomainError
import com.wheretogo.domain.RouteFieldType
import com.wheretogo.domain.handler.CourseAddEvent
import com.wheretogo.domain.handler.CourseAddHandler
import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppScreen
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.model.EventMsg

class CourseAddHandlerImpl(val errorHandler: ErrorHandler) : CourseAddHandler {
    override suspend fun handle(event: CourseAddEvent) {
        when (event) {
            CourseAddEvent.HOME_NAVIGATE ->
                EventBus.send(AppEvent.Navigation(AppScreen.CourseAdd, AppScreen.Home))

            CourseAddEvent.COURSE_ADD_DONE ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.course_add_done)))

            CourseAddEvent.NAME_MIN ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.name_need_two_char)))

            CourseAddEvent.WAYPOINT_MIN ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.need_two_marker_for_path)))

            CourseAddEvent.COURSE_CREATE_NEED ->
                EventBus.send(AppEvent.SnackBar(EventMsg(R.string.course_create_need)))
        }
    }

    override suspend fun handle(error: Throwable): Throwable {
        return when (error) {
            is DomainError.RouteFieldInvalid -> {
                when (error.type) {
                    RouteFieldType.NAME, RouteFieldType.KEYWORD ->
                        EventBus.send(AppEvent.SnackBar(EventMsg(R.string.invalid_name)))

                    RouteFieldType.POINT ->
                        EventBus.send(AppEvent.SnackBar(EventMsg(R.string.click_need_more_marker)))
                }
                error
            }

            else -> {
                errorHandler.handle(error)
            }
        }
    }
}