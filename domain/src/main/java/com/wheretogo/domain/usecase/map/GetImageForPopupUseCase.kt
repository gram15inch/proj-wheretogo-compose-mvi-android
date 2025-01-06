package com.wheretogo.domain.usecase.map

import android.net.Uri

interface GetImageForPopupUseCase {
    suspend operator fun invoke(fileName: String): Uri?
}