package com.wheretogo.domain.usecase.util

import com.wheretogo.domain.model.address.LatLng

interface GetLatLngFromAddressUseCase {
    suspend operator fun invoke(address: String): Result<LatLng>
}