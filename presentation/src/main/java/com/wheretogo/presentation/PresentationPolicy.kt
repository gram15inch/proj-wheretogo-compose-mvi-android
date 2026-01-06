package com.wheretogo.presentation

import android.Manifest
import androidx.annotation.StringRes
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import com.naver.maps.map.NaverMap
import com.naver.maps.map.app.LegalNoticeActivity
import com.naver.maps.map.app.OpenSourceLicenseActivity
import com.wheretogo.domain.DomainError
import com.wheretogo.domain.DriveTutorialStep
import com.wheretogo.domain.RouteAttrItem
import com.wheretogo.domain.model.util.Viewport
import com.wheretogo.presentation.model.EventMsg
import com.wheretogo.presentation.state.CameraState
import javax.inject.Qualifier

const val DRIVE_LIST_MIN_ZOOM = 9.5
const val COURSE_DETAIL_MIN_ZOOM = 10.5
const val COURSE_NAME_MAX_LENGTH = 17
const val WIDE_WIDTH = 600

const val CHECKPOINT_ADD_MARKER = "CHECKPOINT_ADD_MARKER_ID"
const val SEARCH_MARKER = "SEARCH_MARKER_ID"
const val CLEAR_ADDRESS = "CLEAR_ADDRESS"
const val DEBUG_AD_REFRESH_SIZE = 1
const val AD_REFRESH_SIZE = 1
const val AD_MAX_FONT_SCALE = 1.2f

enum class OverlayType {
    SPOT_MARKER, CLUSTER_MARKER, ONE_TIME_MARKER, PATH
}

enum class CommentType(@StringRes val typeRes: Int) {
    ONE(R.string.oneline_review), DETAIL(R.string.detail_review)
}

enum class CameraUpdateSource {
    USER, MARKER,
    LIST_ITEM,
    SEARCH_BAR,
    BOTTOM_SHEET_UP,
    BOTTOM_SHEET_DOWN,
    GUIDE
}

enum class MoveAnimation {
    APP_EASING, APP_LINEAR
}

const val BANNER_URL =
    "https://accurate-flight-2c4.notion.site/1f1cb3833d76805e9f51d663dc940689?pvs=4"

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
    data class NeedSignIn(val msg: String = "") : AppError()
    data class InvalidState(val msg: String = "") : AppError()
    data class Unavailable(val msg: String = "") : AppError()
    data class NetworkError(val msg: String = "") : AppError()
    data class DescriptionEmpty(val msg: String = "") : AppError()
    data class LocationPermissionRequire(val msg: String = "") : AppError()
    data class MapNotSupportExcludeLocation(val msg: String = "") : AppError()
    data class CredentialError(val msg: String = "") : AppError()
    data class Ignore(val msg: String = "") : AppError()
    data class AdLoadError(val msg: String = "") : AppError()
    data class WaitCoolDown(val remainTimeInMinute: Int = 0) : AppError()
    data class UnexpectedException(val throwable: Throwable) : AppError()
}

sealed class AppEvent {
    data class Navigation(val form: AppScreen, val to: AppScreen, val inclusive: Boolean = true) : AppEvent()
    data class SnackBar(val msg: EventMsg) : AppEvent()
    data class Permission(val permission: AppPermission) : AppEvent()
    data object SignInScreen : AppEvent()
}

sealed class AppScreen {
    data object Home : AppScreen()
    data object Drive : AppScreen()
    data object CourseAdd : AppScreen()
    data object Setting : AppScreen()
}

enum class AdMinSize(val widthDp: Int, val heightDp: Int) {
    INVISIBLE(0, 0),
    Row(600, 320),
    Card(300, 600)
}

sealed class AppPermission(val name: String) {
    data object LOCATION : AppPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    data object Unknown : AppPermission("Unknown")
    companion object {
        fun parse(permission: String): AppPermission {
            return when (permission) {
                LOCATION.name -> LOCATION
                else -> Unknown
            }
        }
    }
}

enum class ExportMap {
    NAVER, KAKAO, SKT
}

enum class DriveFloatHighlight {
    NONE, CHECKPOINT, COMMENT, INFO, EXPORT, FOLD
}

enum class HomeBodyBtnHighlight {
    NONE, DRIVE, COURSE_ADD, GUIDE, CREATER_REQUEST
}

enum class HomeBodyBtn {
    DRIVE, COURSE_ADD, GUIDE, CREATER_REQUEST
}

enum class DriveBottomSheetContent(val minHeight: Int) {
    EMPTY(0), COURSE_ADD(80), CHECKPOINT_ADD(0), COURSE_INFO(0), CHECKPOINT_INFO(0), PREVIEW(400)
}

enum class MarkerType {
    DEFAULT, SPOT, CHECKPOINT
}

enum class PathType {
    SCAFFOLD, FULL
}

enum class AppLifecycle {
    onLaunch, onResume, onPause, onDispose, onDestory
}

enum class DriveVisibleMode {
    Explorer, CourseDetail, BlurCourseDetail, BlurCheckpointDetail, SearchBarExpand, BottomSheetExpand, BlurBottomSheetExpand
}

enum class CourseAddVisibleMode {
    BottomSheetExpand, BottomSheetCollapse
}

enum class DriveFloatingVisibleMode {
    Default, Hide, Popup, ExportExpand
}

enum class SheetVisibleMode {
    PartiallyExpand, PartiallyExpanded, Expand, Expanded
}

enum class MarkerZIndex{
   ICON, PHOTO, PHOTO_ZOOM, CLUSTER, ADD
}

fun OverlayType.minZoomLevel(): Double {
    return when (this) {
        OverlayType.SPOT_MARKER -> 8.0
        OverlayType.PATH -> 9.5
        OverlayType.CLUSTER_MARKER -> 9.5
        OverlayType.ONE_TIME_MARKER -> 9.5
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

fun RouteAttrItem?.toStrRes(): Pair<String, Int> {
    return when (this) {
        RouteAttrItem.DRIVE -> Pair("\uD83D\uDCCD", R.string.drive)
        RouteAttrItem.SPORT -> Pair("\uD83C\uDFCE\uFE0F", R.string.sports)
        RouteAttrItem.TRAINING -> Pair("\uD83D\uDD30", R.string.training)

        RouteAttrItem.BEGINNER -> Pair("\uD83C\uDF31", R.string.beginner)
        RouteAttrItem.LOVER -> Pair("\uD83C\uDFC3", R.string.lover)
        RouteAttrItem.EXPERT -> Pair("\uD83C\uDFC7", R.string.expert)
        RouteAttrItem.PRO -> Pair("\uD83D\uDCCD", R.string.pro)

        RouteAttrItem.SOLO -> Pair("\uD83C\uDFCE\uFE0F", R.string.solo)
        RouteAttrItem.FRIEND -> Pair("\uD83E\uDD3C", R.string.friend)
        RouteAttrItem.FAMILY -> Pair("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66", R.string.family)
        RouteAttrItem.COUPLE -> Pair("\uD83D\uDC91", R.string.couple)
        else -> Pair("", R.string.unknown)
    }
}

fun RouteAttrItem?.toIcRes(): Int {
    return when (this) {
        RouteAttrItem.DRIVE -> R.drawable.ic_mk_cr
        RouteAttrItem.SPORT -> R.drawable.ic_mk_sp
        RouteAttrItem.TRAINING -> R.drawable.ic_mk_bg
        else -> R.drawable.ic_mk_df
    }
}

fun DriveTutorialStep.toStrRes(): Int? {
    return when (this) {
        DriveTutorialStep.DRIVE_LIST_ITEM_CLICK -> R.string.welcome_start
        DriveTutorialStep.MOVE_TO_LEAF -> R.string.move_to_spot
        DriveTutorialStep.LEAF_CLICK -> R.string.spot_click
        DriveTutorialStep.COMMENT_FLOAT_CLICK -> R.string.comment_guide
        DriveTutorialStep.EXPORT_FLOAT_CLICK -> R.string.export_guide
        DriveTutorialStep.FOLD_FLOAT_CLICK -> R.string.map_export_click
        DriveTutorialStep.SEARCHBAR_CLICK -> R.string.use_search
        DriveTutorialStep.SEARCHBAR_EDIT -> R.string.display_course_and_place
        DriveTutorialStep.ADDRESS_CLICK -> R.string.blue_course_white_normal
        DriveTutorialStep.DRIVE_GUIDE_DONE -> R.string.safe_drive_with_app
        else -> null
    }
}

fun defaultCommentEmogiGroup(): List<String> {
    return listOf("😊", "😍", "🔥", "👏", "👍", "😂", "🙌", "😮", "🤔", "🤭", "🥹", "😭", "😢", "😡", "😞")
}

fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this
        is DomainError.NetworkError -> AppError.NetworkError(msg)
        is DomainError.UserInvalid -> AppError.NeedSignIn(msg)
        is DomainError.SignInError -> AppError.CredentialError(msg)
        is DomainError.ExpireData -> AppError.InvalidState(msg)
        is DomainError.UserUnavailable -> AppError.Unavailable(msg)
        is DomainError.CoolDownData -> AppError.WaitCoolDown(remainingMinutes)
        is GetCredentialCancellationException -> AppError.Ignore(message?:"")
        is GetCredentialCustomException -> AppError.CredentialError(errorMessage.toString())
        is NoCredentialException -> AppError.CredentialError(errorMessage.toString())
        else -> {
            AppError.UnexpectedException(this)
        }
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher