package com.wheretogo.domain.model.checkpoint

import com.wheretogo.domain.model.user.Profile
import com.wheretogo.domain.model.util.Image

data class CheckPointAddRequest(
    val profile: Profile,
    val image: Image,
    val content: CheckPointContent
) {
    fun valid(): CheckPointAddRequest {
        require(profile.uid.isNotBlank()) { "inValid userId" }
        require(image.imageId.isNotBlank()) { "inValid imageId" }
        require(content.latLng.latitude != 0.0) { "inValid latLng" }
        require(content.latLng.longitude != 0.0) { "inValid latLng" }
        return this
    }
}