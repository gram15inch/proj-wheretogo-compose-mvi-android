package com.wheretogo.presentation

import android.Manifest
import androidx.annotation.StringRes
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.naver.maps.map.NaverMap
import com.naver.maps.map.app.LegalNoticeActivity
import com.naver.maps.map.app.OpenSourceLicenseActivity
import com.wheretogo.domain.RouteAttrItem
import com.wheretogo.domain.UseCaseFailType
import com.wheretogo.domain.model.map.Viewport
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.state.CameraState

const val DRIVE_LIST_MIN_ZOOM = 9.5
const val COURSE_NAME_MAX_LENGTH = 17
const val WIDE_WIDTH = 600

const val CHECKPOINT_ADD_MARKER = "CHECKPOINT_ADD_MARKER_ID"
const val SEARCH_MARKER = "SEARCH_MARKER_ID"
const val CLEAR_ADDRESS = "CLEAR_ADDRESS"


enum class OverlayType {
   SPOT, CHECKPOINT, PATH
}

enum class CommentType(@StringRes val typeRes: Int) {
    ONE(R.string.oneline_review), DETAIL(R.string.detail_review)
}

enum class CameraUpdateSource {
    USER, APP_EASING, APP_LINEAR
}

const val BANNER_URL = "https://accurate-flight-2c4.notion.site/1f1cb3833d76805e9f51d663dc940689?pvs=4"

enum class SettingInfoType(val url: String) {
    PRIVACY("https://accurate-flight-2c4.notion.site/179cb3833d76808b993dc4551d5def8c?pvs=4"),
    LICENCE("https://accurate-flight-2c4.notion.site/179cb3833d768056bfa8e97a3349e0cf?pvs=4"),
    TERMS("https://accurate-flight-2c4.notion.site/179cb3833d768036836dcfc55d8d38aa?pvs=4"),
    GUIDE("https://accurate-flight-2c4.notion.site/17acb3833d7680278027d26f36ce97c6?pvs=4"),
    LegalNotice(LegalNoticeActivity::class.java.name),
    OpenSourceLicense(OpenSourceLicenseActivity::class.java.name)
}

sealed class AppError : Exception() {
    data class ImgEmpty(val msg: String = "") : AppError()
    data class DescriptionEmpty(val msg: String = "") : AppError()
    data class LocationPermissionRequire(val msg: String = "") : AppError()
    data class MapNotSupportExcludeLocation(val msg: String = "") : AppError()
    data class CredentialError(val msg:String = "") : AppError()
    data class Ignore(val msg:String = "") : AppError()
    data class UnexpectedException(val throwable: Throwable): AppError()
}

sealed class AppEvent {
    data class Navigation(val destination: AppScreen) : AppEvent()
    data class SnackBar(val msg: EventMsg) : AppEvent()
    data class Permission(val permission: AppPermission) : AppEvent()
}

sealed class AppScreen {
    data object Home : AppScreen()
    data object Drive : AppScreen()
    data object CourseAdd : AppScreen()
    data object Setting : AppScreen()
}

sealed class AppPermission(val name: String) {
    data object LOCATION : AppPermission(Manifest.permission.ACCESS_FINE_LOCATION)
}

enum class ExportMap{
    NAVER, KAKAO, SKT
}

enum class DriveBottomSheetContent{
   EMPTY ,CHECKPOINT_ADD, INFO
}

enum class MarkerType{
    SPOT, CHECKPOINT
}

enum class PathType{
    PARTIAL, FULL
}

enum class SheetState{
    PartiallyExpand, PartiallyExpanded, Expand, Expanded
}

fun OverlayType.minZoomLevel():Double{
    return when(this){
        OverlayType.SPOT-> 8.0
        OverlayType.PATH-> 9.5
        OverlayType.CHECKPOINT-> 9.5
    }
}

fun NaverMap.toCameraState(): CameraState {
    return contentRegion.run {
        CameraState(
            latLng = cameraPosition.target.toDomainLatLng(),
            zoom = cameraPosition.zoom,
            viewport = Viewport(
                this[0].latitude,
                this[3].latitude,
                this[0].longitude,
                this[3].longitude
            ),
            updateSource = CameraUpdateSource.USER
        )
    }
}

fun RouteAttrItem?.toStrRes():Pair<String,Int>{
    return when(this){
        RouteAttrItem.DRIVE->  Pair("\uD83D\uDCCD", R.string.drive)
        RouteAttrItem.SPORT->  Pair("\uD83C\uDFCE\uFE0F", R.string.sports)
        RouteAttrItem.TRAINING->  Pair("\uD83D\uDD30", R.string.training)

        RouteAttrItem.BEGINNER->  Pair("\uD83C\uDF31", R.string.beginner)
        RouteAttrItem.LOVER->  Pair("\uD83C\uDFC3", R.string.lover)
        RouteAttrItem.EXPERT->  Pair("\uD83C\uDFC7", R.string.expert)
        RouteAttrItem.PRO->  Pair("\uD83D\uDCCD", R.string.pro)

        RouteAttrItem.SOLO->  Pair("\uD83C\uDFCE\uFE0F", R.string.solo)
        RouteAttrItem.FRIEND->  Pair("\uD83E\uDD3C", R.string.friend)
        RouteAttrItem.FAMILY->  Pair("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66", R.string.family)
        RouteAttrItem.COUPLE->  Pair("\uD83D\uDC91", R.string.couple)
        else-> Pair("", R.string.unknown)
    }
}

fun RouteAttrItem?.toIcRes():Int{
    return when(this){
        RouteAttrItem.DRIVE-> R.drawable.ic_mk_cr
        RouteAttrItem.SPORT->  R.drawable.ic_mk_sp
        RouteAttrItem.TRAINING->  R.drawable.ic_mk_bg
        else-> R.drawable.ic_mk_df
    }
}

fun getCommentEmogiGroup(): List<String> {
    return listOf("😊", "😍", "🔥", "👏", "👍", "😂", "🙌", "😮", "🤔", "🤭", "🥹", "😭", "😢", "😡", "😞")
}

fun UseCaseFailType.toStringRes():Int{
    return when(this){
        UseCaseFailType.USER_CREATE_ERROR ->{ R.string.user_create_error }
        UseCaseFailType.INVALID_USER ->{  R.string.invalid_user }
        UseCaseFailType.GOOGLE_AUTH ->{  R.string.google_auth }
        UseCaseFailType.NETWORK_ERROR ->{  R.string.network_error }
        UseCaseFailType.INVALID_DATA ->{  R.string.invalid_data }
    }
}

fun Throwable.toAppError():AppError{
    return when(this){
        is AppError -> this
        is GetCredentialCancellationException -> AppError.Ignore()
        is GetCredentialCustomException -> AppError.CredentialError()
        is NoCredentialException -> AppError.CredentialError()
        else -> {
            if(!BuildConfig.DEBUG)
                FirebaseCrashlytics.getInstance().recordException(this)
            AppError.UnexpectedException(this)
        }
    }
}

fun AppError.toStringRes():Int?{
    return when(this){
        is AppError.CredentialError -> R.string.network_error
        is AppError.Ignore -> null
        else-> R.string.unexpect_error
    }
}