package com.wheretogo.domain.model.checkpoint


data class CheckPointAddRequest(
    val content: CheckPointContent,
    val imageId: String,
    val thumbnail: String
) {
    fun valid(): CheckPointAddRequest {
        require(content.latLng.latitude != 0.0) { "inValid latLng" }
        require(content.latLng.longitude != 0.0) { "inValid latLng" }
        require(content.groupId.isNotBlank())
        { "inValid courseId: ${content.groupId}" }
        require(imageId.isNotBlank() && imageId.startsWith("IM"))
        { "inValid imageId: $imageId" }
        return this
    }
}