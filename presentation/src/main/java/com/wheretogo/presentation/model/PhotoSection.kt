package com.wheretogo.presentation.model

import com.wheretogo.domain.model.gallery.GalleryPhoto

data class PhotoSection(
    val key: String,
    val title: String,
    val hasStamp: Boolean,
    val photos: List<GalleryPhoto>,
)