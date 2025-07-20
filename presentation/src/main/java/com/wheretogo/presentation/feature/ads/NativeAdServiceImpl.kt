package com.wheretogo.presentation.feature.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.presentation.AdLifecycle
import com.wheretogo.presentation.AppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Deque
import java.util.LinkedList
import javax.inject.Inject

@Suppress("MissingPermission")
class NativeAdServiceImpl @Inject constructor (val context: Context, val adId: String):AdService {
    private val _mutexAdCache = Mutex()
    private val _nativeAdCache: Deque<NativeAd> = LinkedList()
    private val refreshAdNumbers = 3

    private var serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var _error: Throwable? = null
    private var _refreshTime: Long = 0
    private var _errorCount = 0

    private val _adLifeCycle = MutableSharedFlow<AdLifecycle>()
    override val adLifeCycle: Flow<AdLifecycle> get() = _adLifeCycle

    override suspend fun getAd():Result<List<NativeAd>>{
        val isLoadSuccess= serviceScope.async {
            when {
                _refreshTime != 0L -> {
                    return@async _nativeAdCache.waitUntilNotEmpty()
                }

                _nativeAdCache.isEmpty() -> {
                    refreshAd()
                    return@async _nativeAdCache.waitUntilNotEmpty()
                }

                _nativeAdCache.size in (1..5) -> {
                    refreshAd()
                    return@async Result.success(true)
                }
                else->{
                    return@async Result.success(true)
                }
            }
        }

        isLoadSuccess.await().onFailure {
            serviceScope.cancel()
            serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            return@getAd Result.failure(it)
        }

        val adGroup= _nativeAdCache.pollByMutex()
       return Result.success(adGroup)
    }

    override fun refreshAd(){
        serviceScope.launch(Dispatchers.IO) {
            if(_refreshTime == 0L) {
                _refreshTime = System.currentTimeMillis()
                val requestRefreshTime = _refreshTime
                val callbackCoroutine = CoroutineScope(coroutineContext + SupervisorJob())
                adLoad(
                    onSuccess = {
                        callbackCoroutine.launch {
                            _nativeAdCache.offerLast(it)
                            _errorCount = 0
                            _refreshTime = 0L
                        }
                    },
                    onFail = {
                        callbackCoroutine.launch {
                            _errorCount++
                            if(_errorCount>=refreshAdNumbers)
                                _error = AppError.AdLoadError(it.message)
                            _refreshTime = 0L
                        }
                    }
                )

                launch {
                    repeat(15){
                        delay(1000)
                        if(_refreshTime==0L)
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

    override fun lifeCycleChange(event: AdLifecycle) {
        serviceScope.launch {
            _adLifeCycle.emit(event)
        }
    }

    private fun adLoad(onSuccess: (NativeAd) -> Unit, onFail: (LoadAdError) -> Unit) {
        return AdLoader.Builder(context, adId).forNativeAd {
            onSuccess(it)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onFail(adError)
            }
        }).build().loadAds(AdRequest.Builder().build(), refreshAdNumbers)
    }

    private suspend fun Deque<NativeAd>.pollByMutex():List<NativeAd>{
        return _mutexAdCache.withLock {
            val adGroup= poll().run {
                if(this==null)
                    emptyList()
                else
                    listOf(this)
            }
            adGroup
        }
    }

    private suspend fun Deque<NativeAd>.waitUntilNotEmpty():Result<Unit>{
        repeat(20) {
            delay(500)
            when{
                isNotEmpty() -> return Result.success(Unit)
                _error!=null -> {
                    val errorCopy = _error!!
                    _error=null
                    return Result.failure(errorCopy)
                }
            }
        }
        return Result.failure(AppError.AdLoadError("timeout"))
    }
}