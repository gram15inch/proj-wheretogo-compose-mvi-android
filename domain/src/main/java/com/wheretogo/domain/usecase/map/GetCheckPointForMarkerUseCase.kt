package com.wheretogo.domain.usecase.map

import com.wheretogo.domain.model.map.CheckPoint
import com.wheretogo.domain.model.map.MetaCheckPoint

interface GetCheckPointForMarkerUseCase {
    suspend operator fun invoke(metaCheckPoint: MetaCheckPoint): List<CheckPoint>
}