package com.wheretogo.domain.model.gallery

data class GalleryPhoto(
    val id: Long, // 시스템 picker id
    val imageId: String = "",
    val uriString: String,
    val thumbnail:String = "",
    val courseId:String? = null,
    val courseName:String? = null,
    val exif: PhotoExif,
)