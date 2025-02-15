package com.wheretogo.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wheretogo.domain.CourseDetail

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

enum class AppEvent{
    NAVIGATION, SNACKMAR
}

enum class ExportMap{
    NAVER, KAKAO, SKT
}

enum class MarkerIconType(@DrawableRes val res: Int) {
    DEFAULT(R.drawable.ic_mk_df),
    CAR(R.drawable.ic_mk_cr),
    RACING(R.drawable.ic_mk_sp),
    TRAINING(R.drawable.ic_mk_bg),
    PHOTO(R.drawable.ic_mk_cm)
}

fun getCourseIconType(courseType:String):MarkerIconType{
    return  try {
        when(CourseDetail.fromCode(courseType)){
            CourseDetail.DRIVE->{ MarkerIconType.CAR }
            CourseDetail.SPORT->{  MarkerIconType.RACING  }
            CourseDetail.TRAINING->{ MarkerIconType.TRAINING}
            else->{  MarkerIconType.DEFAULT }
        }
    }catch (e:Exception){
        MarkerIconType.DEFAULT
    }
}