package com.dhkim139.feature.camerapicker.usecase

import android.net.Uri
import com.dhkim139.feature.camerapicker.model.Location
import com.dhkim139.feature.camerapicker.model.VerifiedImage

interface VerifyImageUseCase {
    suspend operator fun invoke(
        uri: Uri,
        latestLocation: Location?,
        returnedAt: Long,
    ): VerifiedImage
}
