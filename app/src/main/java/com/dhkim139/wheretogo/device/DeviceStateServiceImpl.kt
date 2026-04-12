package com.dhkim139.wheretogo.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.wheretogo.domain.feature.DeviceState
import com.wheretogo.domain.feature.DeviceStateService
import com.wheretogo.domain.feature.NetworkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DeviceStateServiceImpl(
    private val context: Context,
    private val scope: CoroutineScope
) : DeviceStateService {

    private val _networkType = MutableStateFlow(getCurrentNetworkType())

    override val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    override val deviceState: StateFlow<DeviceState> = networkType
        .map { DeviceState(networkType = it) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = DeviceState()
        )

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkType.value = getCurrentNetworkType()
            }

            override fun onLost(network: Network) {
                _networkType.value = NetworkType.NONE
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                _networkType.value = getCurrentNetworkType()
            }
        }

        cm.registerDefaultNetworkCallback(callback)
    }

    private fun getCurrentNetworkType(): NetworkType {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.activeNetwork
            ?.let { cm.getNetworkCapabilities(it) }
            ?: return NetworkType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            else -> NetworkType.NONE
        }
    }
}