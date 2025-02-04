package com.wheretogo.presentation

import androidx.annotation.StringRes

enum class CommentType(@StringRes val typeRes: Int) {
    ONE(R.string.oneline_review), DETAIL(R.string.detail_review)
}

enum class CameraStatus {
    NONE, TRACK, INIT
}

enum class SettingInfoType(val url: String) {
    PRIVACY("https://accurate-flight-2c4.notion.site/179cb3833d76808b993dc4551d5def8c?pvs=4"),
    LICENCE("https://accurate-flight-2c4.notion.site/179cb3833d768056bfa8e97a3349e0cf?pvs=4"),
    TERMS("https://accurate-flight-2c4.notion.site/179cb3833d768036836dcfc55d8d38aa?pvs=4"),
    GUIDE("https://accurate-flight-2c4.notion.site/17acb3833d7680278027d26f36ce97c6?pvs=4"),
}

enum class CheckPointAddError{
    EMPTY_IMG, EMPTY_DESCRIPTION
}

enum class ViewModelEvent{
    NAVIGATION, TOAST
}

enum class ExportMap{
    NAVER, KAKAO, SKT
}