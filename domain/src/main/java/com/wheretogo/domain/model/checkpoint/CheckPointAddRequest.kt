package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.util.ImageUris


data class CheckPointAddRequest(
    val imageUris: ImageUris,
    val content: CheckPointCreateContent
) {
    fun valid(): CheckPointAddRequest {
        require(imageUris.imageId.isNotBlank()) { "inValid imageId" }
        require(content.latLng.latitude != 0.0) { "inValid latLng" }
        require(content.latLng.longitude != 0.0) { "inValid latLng" }
        return this
    }
}