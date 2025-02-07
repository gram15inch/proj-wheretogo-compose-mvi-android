package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.LatLng

interface GetLatLngFromAddressUseCase {
    suspend operator fun invoke(address : String): UseCaseResponse<LatLng>
}