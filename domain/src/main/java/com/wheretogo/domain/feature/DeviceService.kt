package com.wheretogo.domain.feature

import kotlinx.coroutines.flow.StateFlow

data class DeviceState(
    val networkType: NetworkType = NetworkType.NONE
)

enum class NetworkType {
    WIFI, CELLULAR, NONE
}

interface DeviceStateService {
    val deviceState: StateFlow<DeviceState>
    val networkType: StateFlow<NetworkType>
}