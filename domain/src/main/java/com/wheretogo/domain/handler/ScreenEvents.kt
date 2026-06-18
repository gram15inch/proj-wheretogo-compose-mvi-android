package com.wheretogo.domain.handler


enum class DriveMsgEvent {
    ADD_DONE, REMOVE_DONE, REPORT_DONE, UNKNOWN_ERR
}

enum class HomeEvent {
    DRIVE_NAVIGATE, COURSE_ADD_NAVIGATE, GALLERY_NAVIGATE, GUIDE_START, GUIDE_STOP
}

enum class CourseAddEvent {
    HOME_NAVIGATE, COURSE_ADD_DONE, NAME_MIN, COURSE_CREATE_NEED, WAYPOINT_MIN
}

enum class LoginEvent {
    SIGN_IN_SUCCESS, SIGN_IN_FAIL
}

enum class RootEvent {
    APP_CHECK_SUCCESS, USER_CHECK_SUCCESS, ACCOUNT_VALID_EXPIRE
}

enum class GalleryFlowMsgEvent {
    GALLERY_LOAD_FAIL, MEDIA_SAVE_FAIL, PHOTO_DELETE_FAIL
}
