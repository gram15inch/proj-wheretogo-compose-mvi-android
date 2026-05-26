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

    // 디버깅용
    fun toCheckPointForTest(): CheckPoint{
        val now = System.currentTimeMillis()
        return CheckPoint(
            checkPointId = "CP${now}",
            courseId = content.groupId,
            userId = "TestUserId",
            userName = "TestUser",
            latLng = content.latLng,
            captionId = "",
            caption = "",
            imageId = imageId,
            thumbnail = thumbnail,
            description = "",
            isUserCreated = true,
            isHide = false,
            reportedCount = 0,
            updateAt = now,
            createAt = now
        )
    }
}