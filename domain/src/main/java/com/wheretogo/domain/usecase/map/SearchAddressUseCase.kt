package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.UseCaseResponse
import com.wheretogo.domain.model.map.SimpleAddress

interface SearchAddressUseCase {
    suspend operator fun invoke(query: String): UseCaseResponse<List<SimpleAddress>>
}