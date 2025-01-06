package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.map.CheckPoint

interface GetCheckpointForMarkerUseCase {
    suspend operator fun invoke(courseId: String): List<CheckPoint>
}