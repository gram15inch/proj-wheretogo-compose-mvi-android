package com.wheretogo.domain.model.home

import com.wheretogo.domain.RecentCardSituation
import com.wheretogo.domain.model.gallery.GalleryPhoto

data class RecentCard(
    val latestPhoto: GalleryPhoto?,
    val situation: RecentCardSituation
)