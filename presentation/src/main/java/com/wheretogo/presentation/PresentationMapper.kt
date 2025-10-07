package com.wheretogo.presentation


import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.SearchType
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.checkpoint.CheckPointContent
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentContent
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseContent
import com.wheretogo.domain.model.route.RouteCategory
import com.wheretogo.domain.model.util.Navigation
import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.model.AppMarker
import com.wheretogo.presentation.model.MapOverlay
import com.wheretogo.presentation.model.MarkerInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.naver.maps.geometry.LatLng as NaverLatLng

fun List<NativeAd>.toItem(): List<AdItem> {
    return this.map {
        AdItem(it)
    }
}

fun List<LatLng>.toNaver(): List<NaverLatLng> {
    return this.map { NaverLatLng(it.latitude, it.longitude) }
}

fun NaverLatLng.toDomainLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.toNaver(): NaverLatLng {
    return NaverLatLng(latitude, longitude)
}

fun SimpleAddress.toSearchBarItem(): SearchBarItem {
    return SearchBarItem(
        label = title,
        address = address,
        latlng = latlng,
        isCourse = type == SearchType.COURSE
    )
}

fun RouteAttr.toStrRes(): Int {
    return when (this) {
        RouteAttr.TYPE -> R.string.category
        RouteAttr.LEVEL -> R.string.level
        RouteAttr.RELATION -> R.string.recommend
    }
}

fun Course.toNavigation(): Navigation {
    return Navigation(
        courseName = courseName,
        waypoints = waypoints
    )
}

fun CourseAddScreenState.CourseAddSheetState.toCourseContent(
    cameraLatLng: LatLng? = null,
    zoom: String = ""
): CourseContent {
    val waypoint = routeState.waypointItemStateGroup.map { it.data.latlng }
    val points = routeState.points
    val duration = (routeState.duration / 60000).toString()
    val type = selectedCategoryCodeGroup.get(RouteAttr.TYPE).toString()
    val level = selectedCategoryCodeGroup.get(RouteAttr.LEVEL).toString()
    val relation = selectedCategoryCodeGroup.get(RouteAttr.RELATION).toString()
    return CourseContent(
        courseName = courseName,
        waypoints = waypoint,
        points = points,
        duration = duration,
        type = type,
        level = level,
        relation = relation,
        cameraLatLng = cameraLatLng ?: waypoint.firstOrNull() ?: LatLng(),
        zoom = zoom
    )
}

fun CheckPointAddState.toCheckPointContent(courseId: String): CheckPointContent {
    return CheckPointContent(
        courseId = courseId,
        latLng = latLng,
        imageUriString = imgUriString,
        description = description
    )
}

fun CommentAddState.toCommentContent(groupId: String, editText: String): CommentContent {
    return CommentContent(
        groupId = groupId,
        emoji = this.largeEmoji.ifEmpty { emogiGroup.firstOrNull() ?: "" },
        oneLineReview = if (CommentType.ONE == commentType) editText else this.oneLineReview,
        detailedReview = if (CommentType.DETAIL == commentType) editText else this.detailReview
    )
}


fun Comment.toCommentItemState(): CommentState.CommentItemState {
    return CommentState.CommentItemState(
        this,
        !isFocus && detailedReview.length > 10
    )
}

fun parseLogoImgRes(company: String): Int {
    val auth = try {
        AuthCompany.valueOf(company)
    } catch (_: Exception) {
        AuthCompany.GOOGLE
    }

    return when (auth) {
        AuthCompany.GOOGLE -> {
            R.drawable.lg_app
        }

        else -> {
            R.drawable.lg_app
        }
    }
}

fun Course.toMarkerContainer(marker: AppMarker): MapOverlay.MarkerContainer {
    return MapOverlay.MarkerContainer(
        courseId, MarkerType.SPOT,
        marker
    )
}

fun Course.toMarkerInfo(): MarkerInfo {
    return MarkerInfo(
        contentId = courseId,
        position = waypoints.first(),
        iconRes = RouteCategory.fromCode(type)?.item.toIcRes()
    )
}

fun CheckPoint.toMarkerContainer(marker: AppMarker): MapOverlay.MarkerContainer {
    return MapOverlay.MarkerContainer(
        courseId, MarkerType.CHECKPOINT,
        marker
    )
}

fun CheckPoint.toMarkerInfo(): MarkerInfo {
    val icon = when (checkPointId) {
        CHECKPOINT_ADD_MARKER -> R.drawable.ic_mk_cm
        SEARCH_MARKER -> R.drawable.ic_mk_df
        else -> null
    }
    return MarkerInfo(
        contentId = checkPointId,
        position = latLng,
        caption = caption,
        iconPath = thumbnail.ifEmpty { null },
        iconRes = icon
    )
}
