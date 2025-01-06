package com.wheretogo.domain.model.map

import android.net.Uri

data class CheckPointAddRequest(
    val courseId: String = "",
    val checkpointIdGroup: List<String> = emptyList(),
    val latLng: LatLng = LatLng(),
    val imageName: String = "",
    val imageUri: Uri? = null,
    val description: String = ""
)
