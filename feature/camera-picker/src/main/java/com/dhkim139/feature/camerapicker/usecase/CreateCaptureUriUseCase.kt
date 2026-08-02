package com.dhkim139.feature.camerapicker.usecase

import android.net.Uri


interface CreateCaptureUriUseCase {
    operator fun invoke(): Uri
}

