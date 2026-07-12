package com.wheretogo.domain.model.gallery

data class GalleryPhoto(
    val id: Long,
    val exif: PhotoExif,
    val imageId: String,
    val sha256: String,
    val entityId: Long,
    val imageSource: String? = null, // uriString, 파이어베이스 스토리지 상대 경로
    val thumbnail: String? = null,   // 로컬 uri 주소
    val courseId: String? = null,
    val courseName: String? = null,
    val address: String? = null,
    val isStampedGroup: Boolean? = null
){
    fun simpleAddress(drop:Int = 1, take:Int = 2) : String? =
        address?.split(" ")
            ?.drop(drop)
            ?.take(take)
            ?.joinToString(" ")
}