package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.util.FilePreview


interface GetImageUseCase {
    suspend operator fun invoke(imageId: String): String?
    suspend fun getPreview(imageUriString: String): Result<FilePreview>
}