package com.wheretogo.presentation


import com.dhkim139.feature.camerapicker.model.VerifiedImageGroup
import com.dhkim139.feature.providerpicker.model.ProviderPickerItem
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.MarkerType
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.SearchType
import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.address.SimpleAddress
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.comment.CommentContent
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.course.CourseContent
import com.wheretogo.domain.model.course.CourseRenderItem
import com.wheretogo.domain.model.course.StartDirection
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.route.Direction
import com.wheretogo.domain.model.route.RouteCategory
import com.wheretogo.domain.model.util.Navigation
import com.wheretogo.presentation.model.LeafInfo
import com.wheretogo.presentation.model.MiniPhoto
import com.wheretogo.presentation.model.PickedImage
import com.wheretogo.presentation.model.PolylineInfo
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CommentState
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.wheretogo.presentation.state.CourseAddScreenState
import com.naver.maps.geometry.LatLng as NaverLatLng

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

fun CourseRenderItem.toNavigation(): Navigation {
    val waypoints = if(direction == StartDirection.FORWARD) fWaypoint else bWaypoint
    return Navigation(
        courseName = title,
        waypoints = waypoints,
        direction = direction
    )
}

fun CommentAddState.toCommentContent(editText: String): CommentContent {
    return CommentContent(
        emoji = this.titleEmoji.ifEmpty { emogiGroup.firstOrNull() ?: "" },
        oneLineReview = if (CommentType.ONE == commentType) editText else this.oneLineReview,
        detailedReview = if (CommentType.DETAIL == commentType) editText else this.detailReview
    )
}


fun Comment.toItemState(): CommentState.CommentItemState {
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

fun Course.toMarkerInfo(): MarkerInfo? {
    val pos = fWaypoints.firstOrNull()
    pos?:return null
    return MarkerInfo(
        contentId = id,
        position = pos,
        type = MarkerType.COURSE,
        iconRes = RouteCategory.fromCode(type)?.item.toIcRes()
    )
}

fun Course.toForwardLine(): PolylineInfo {
    return PolylineInfo(
        contentId = id,
        direction = Direction.FORWARD,
        points = forward
    )
}

fun Course.toBackwardLine(): PolylineInfo {
    return PolylineInfo(
        contentId = id,
        direction = Direction.BACKWARD,
        points = backward
    )
}

fun CheckPoint.toLeafInfo(): LeafInfo {
    return LeafInfo(
        id = checkPointId,
        latLng = latLng,
        caption = caption,
        thumbnail = thumbnail
    )
}

fun GalleryPhoto.toMiniPhoto(): MiniPhoto {
    return MiniPhoto(
        id = id,
        photoUri = thumbnail,
        courseName = courseName?:""
    )
}

fun ProviderPickerItem.toPickedImageGroup() = PickedImage(
    id = id,
    uri = uri,
    latLng = null
)

fun VerifiedImageGroup.toPickedImageGroup() =
    images.map { image ->
        PickedImage(
            id = image.id,
            uri = image.uri,
            latLng = LatLng().let {
                val latLng = image.location
                if (latLng != null)
                    it.copy(
                        latLng.lat,
                        latLng.lng
                    )
                else
                    null
            }
        )
    }
