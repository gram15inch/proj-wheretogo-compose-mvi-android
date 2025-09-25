package com.wheretogo.presentation.feature.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.presentation.AppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Deque
import java.util.LinkedList
import javax.inject.Inject

@Suppress("MissingPermission")
class NativeAdServiceImpl @Inject constructor(
    val context: Context,
    val adId: String,
    val refreshAdNumbers: Int
) : AdService {
    private val _mutexAdCache = Mutex()
    private val _nativeAdCache: Deque<NativeAd> = LinkedList()

    private var serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var _error: Throwable? = null
    private var _refreshTime: Long = 0
    private var _errorCount = 0

    override suspend fun getAd(): Result<List<NativeAd>> {
        val isLoadSuccess = serviceScope.async {
            when {
                _refreshTime != 0L -> {
                    return@async _nativeAdCache.waitUntilNotEmpty()
                }

                _nativeAdCache.isEmpty() -> {
                    refreshAd(refreshAdNumbers)
                    return@async _nativeAdCache.waitUntilNotEmpty()
                }

                _nativeAdCache.size == 1 -> {
                    refreshAd(refreshAdNumbers)
                    return@async Result.success(true)
                }

                else -> {
                    return@async Result.success(true)
                }
            }
        }

        isLoadSuccess.await().onFailure {
            serviceScope.cancel()
            serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            return@getAd Result.failure(it)
        }

        val adGroup = _nativeAdCache.pollByMutex()
        return Result.success(adGroup)
    }

    override fun refreshAd(count: Int) {
        serviceScope.launch(Dispatchers.IO) {
            if (_refreshTime == 0L) {
                _refreshTime = System.currentTimeMillis()
                val requestRefreshTime = _refreshTime
                val callbackCoroutine = CoroutineScope(coroutineContext + SupervisorJob())
                adLoad(
                    request = count,
                    onSuccess = {
                        callbackCoroutine.launch {
                            _nativeAdCache.offerLast(it)
                            _errorCount = 0
                            _refreshTime = 0L
                        }
                    }
                ) {
                    callbackCoroutine.launch {
                        _errorCount++
                        if (_errorCount >= count)
                            _error = AppError.AdLoadError(it.message)
                        _refreshTime = 0L
                    }
                }

                launch {
                    repeat(15) {
                        delay(1000)
                        if (_refreshTime == 0L)
                            return@launch
                    }
                    if (_refreshTime == requestRefreshTime) {
                        callbackCoroutine.cancel()
                        _error = AppError.AdLoadError("refresh timeout")
                        _refreshTime = 0L
                    }
                }
            }
        }
    }

    private fun adLoad(request: Int, onSuccess: (NativeAd) -> Unit, onFail: (LoadAdError) -> Unit) {
        return AdLoader.Builder(context, adId).forNativeAd {
            onSuccess(it)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onFail(adError)
            }
        }).build().loadAds(AdRequest.Builder().build(), request)
    }

    private suspend fun Deque<NativeAd>.pollByMutex(): List<NativeAd> {
        return _mutexAdCache.withLock {
            val adGroup = poll().run {
                if (this == null)
                    emptyList()
                else
                    listOf(this)
            }
            adGroup
        }
    }

    private suspend fun Deque<NativeAd>.waitUntilNotEmpty(): Result<Unit> {
        repeat(20) {
            delay(500)
            when {
                isNotEmpty() -> return Result.success(Unit)
                _error != null -> {
                    val errorCopy = _error!!
                    _error = null
                    return Result.failure(errorCopy)
                }
            }
        }
        return Result.failure(AppError.AdLoadError("timeout"))
    }
}