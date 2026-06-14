package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.address.LatLng


data class NearLatLng(
    val percent: Float,
    val latlng: LatLng
)
interface GetImageUseCase {
    suspend operator fun invoke(imageId: String): String?
    suspend fun getNearLatLng(imageUriString: String): Result<NearLatLng?>
}