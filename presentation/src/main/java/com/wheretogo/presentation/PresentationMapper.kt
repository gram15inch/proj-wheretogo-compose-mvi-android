package com.wheretogo.presentation


import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.domain.AuthCompany
import com.wheretogo.domain.RouteAttr
import com.wheretogo.domain.model.map.Comment
import com.wheretogo.domain.model.map.LatLng
import com.wheretogo.domain.model.map.SimpleAddress
import com.wheretogo.presentation.model.AdItem
import com.wheretogo.presentation.model.SearchBarItem
import com.wheretogo.presentation.state.CommentState.CommentAddState
import com.naver.maps.geometry.LatLng as NaverLatLng

fun List<NativeAd>.toItem():List<AdItem>{
    return this.map {
        AdItem(it)
    }
}

fun List<LatLng>.toNaver(): List<NaverLatLng> {
    return this.map { NaverLatLng(it.latitude, it.longitude) }
}


fun List<NaverLatLng>.toDomain():List<LatLng>{
    return map{it.toDomainLatLng()}
}

fun NaverLatLng.toDomainLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun LatLng.toNaver(): NaverLatLng {
    return NaverLatLng(latitude, longitude)
}

fun SimpleAddress.toSearchBarItem():SearchBarItem{
    return SearchBarItem(label = title, address = address, latlng = latlng)
}

fun CommentAddState.toComment(): Comment {
    return Comment(
        groupId = this.groupId,
        emoji = this.largeEmoji.ifEmpty { emogiGroup.firstOrNull() ?: "" },
        oneLineReview = if (CommentType.ONE == commentType) editText.text else this.oneLineReview,
        detailedReview = if (CommentType.DETAIL == commentType) editText.text else this.detailReview,
        date = System.currentTimeMillis()
    )
}

fun RouteAttr.toStrRes(): Int {
    return when (this) {
        RouteAttr.TYPE -> R.string.category
        RouteAttr.LEVEL -> R.string.level
        RouteAttr.RELATION -> R.string.recommend
        else -> R.string.unknown
    }
}


fun parseLogoImgRes(company: String): Int {
    val auth = try {
        AuthCompany.valueOf(company)
    } catch (e: Exception) {
        AuthCompany.GOOGLE
    }

    return when (auth) {
        AuthCompany.GOOGLE -> {
            R.drawable.lg_app
        }
        else->{
            R.drawable.lg_app
        }
    }
}
