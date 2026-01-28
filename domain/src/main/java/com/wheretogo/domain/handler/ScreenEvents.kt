package com.wheretogo.domain.handler


enum class DriveEvent {
    ADD_DONE, REMOVE_DONE, REPORT_DONE
}

enum class HomeEvent {
    DRIVE_NAVIGATE, COURSE_ADD_NAVIGATE, GUIDE_START, GUIDE_STOP
}

enum class CourseAddEvent {
    HOME_NAVIGATE, COURSE_ADD_DONE, NAME_MIN, COURSE_CREATE_NEED, WAYPOINT_MIN
}

enum class LoginEvent {
    SIGN_IN_SUCCESS, SIGN_IN_FAIL
}

enum class RootEvent {
    APP_CHECK_SUCCESS, ACCOUNT_VALID_EXPIRE
}
