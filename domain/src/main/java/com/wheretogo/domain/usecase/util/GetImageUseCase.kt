package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.address.LatLng
import com.wheretogo.domain.model.util.FilePreview


data class NearLatLng(
    val percent: Float,
    val latlng: LatLng
)
interface GetImageUseCase {
    suspend operator fun invoke(imageId: String): String?
    suspend fun getNearLatLng(imageUriString: String): Result<NearLatLng?>
    suspend fun getPreview(imageUriString: String): Result<FilePreview>
}