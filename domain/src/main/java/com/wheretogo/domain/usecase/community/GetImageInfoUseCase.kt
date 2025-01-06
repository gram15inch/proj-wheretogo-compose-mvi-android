package com.wheretogo.domain.usecase.community

import android.net.Uri
import com.wheretogo.domain.model.community.ImageInfo

interface GetImageInfoUseCase {
    suspend operator fun invoke(imgUri: Uri): ImageInfo
}