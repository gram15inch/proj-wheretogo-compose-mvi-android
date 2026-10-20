package com.wheretogo.presentation.feature.ads

import android.Manifest
import android.content.Context
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.wheretogo.presentation.AppError
import com.wheretogo.presentation.PresentationBuildConfig
import com.wheretogo.presentation.model.AdItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class NativeAdConfig(
    val minRefreshIntervalMs: Long = 60_000L,      // 슬롯 광고 교체 주기
    val minRequestIntervalMs: Long = 5_000L,       // 네트워크 요청 간 최소 간격
    val loadTimeoutMs: Long = 10_000L,             // 광고 확보 대기 한도
    val adExpireMs: Long = 60 * 60 * 1000L,        // 광고 유효시간 (1시간), 로드된지 오래된 광고 표시 방지
    val preloadCount: Int = 2,                     // 한 번에 받을 광고 수 (최대 5)
)

@Suppress("MissingPermission")
class NativeAdServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adId: String,
    private val config: PresentationBuildConfig,
    private val adConfig: NativeAdConfig
) : AdService {

    private data class Slot(val ad: AdItem, val createAt: Long)

    private val lock = Any()
    private val cache = ArrayDeque<AdItem>()
    private val slots = HashMap<String, Slot>()
    private var inFlight: CompletableDeferred<Result<Unit>>? = null
    private var lastRequestTime = 0L
    private var loadDisabled = !config.isAdLoad

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override suspend fun getAd(slotId: String): Result<List<AdItem>> {
        val slot = synchronized(lock) {
            if (loadDisabled) return Result.success(emptyList())
            slots[slotId]
        }

        // 광고, 슬롯 만료 검사
        if (slot != null && !isAdExpired(slot.ad) && !isSlotExpired(slot)) {
            return Result.success(listOf(slot.ad))
        }

        // 새 광고 갱신
        val newAd = withTimeoutOrNull(adConfig.loadTimeoutMs.milliseconds) { acquireAd() }
        if(loadDisabled){ // 비활성화시 광고 버림
            newAd?.destroy()
            return  Result.success(emptyList())
        }

        // 광고 슬롯 할당
        if (newAd != null) {
            val result = safeAssignSlot(slotId, slot, newAd)
            if (result != null) return Result.success(listOf(result))
        }

        // 갱신 실패시 기존 슬롯 반환
        if (slot != null && !isAdExpired(slot.ad)) {
            return Result.success(listOf(slot.ad))
        }
        return Result.failure(AppError.AdLoadError("no ad available"))
    }

    override fun clearSlot(slotId: String) {
        synchronized(lock) { slots.remove(slotId) }?.ad?.destroy()
    }

    override fun disable() {
        val toDestroy = synchronized(lock) {
            loadDisabled = true
            inFlight = null
            val all = slots.values.map { it.ad } + cache
            slots.clear()
            cache.clear()
            all
        }
        serviceScope.cancel()
        toDestroy.forEach { it.destroy() }
    }

    private fun safeAssignSlot(slotId: String, target: Slot?, newAd: AdItem): AdItem? {
        var old: AdItem? = null
        val assigned = synchronized(lock) {
            when {
                slots[slotId] !== target -> {
                    cache.addFirst(newAd) // 다른 호출이 먼저 교체시 사용하지 않고 저장
                    slots[slotId]?.ad
                }
                else -> {
                    old = target?.ad
                    slots[slotId] = Slot(newAd, now()) // 교체
                    newAd
                }
            }
        }
        old?.destroy()   // 데드락 대비 밖에서 호출
        return assigned
    }

    private suspend fun acquireAd(): AdItem? {
        while (true) {
            cache.pollValidAd()?.let { ad ->
                if (cacheSize() == 0) prefetch()
                return ad
            }

            val waiter = startLoadIfAllowed()
            if (waiter == null) {
                delay(remainingRequestIntervalMs().coerceAtLeast(100L).milliseconds)
                continue
            }

            // 성공했는데 다른 호출이 먼저 가져갔으면 루프에서 다시 확인
            if (waiter.await().isFailure) return null
        }
    }

    private fun prefetch() {
        serviceScope.launch { startLoadIfAllowed() }
    }

    /** 요청 간격이 지났을 때만 새 요청. 진행 중인 요청이 있으면 그걸 반환 */
    private suspend fun startLoadIfAllowed(): CompletableDeferred<Result<Unit>>? {
        val deferred = synchronized(lock) {
            if (loadDisabled) {
                return CompletableDeferred(Result.failure(AppError.AdLoadError("released")))
            }
            inFlight?.let { return it }
            val now = now()
            if (lastRequestTime != 0L && now - lastRequestTime < adConfig.minRequestIntervalMs) {
                return null
            }
            lastRequestTime = now
            CompletableDeferred<Result<Unit>>().also { inFlight = it }
        }
        // 호출 쪽이 취소돼도 inFlight가 걸린 채 남지 않도록 NonCancellable
        withContext(NonCancellable + Dispatchers.Main) { adLoad(deferred) }
        return deferred
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    private fun adLoad(deferred: CompletableDeferred<Result<Unit>>) {
        lateinit var loader: AdLoader
        loader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                val accepted = synchronized(lock) {
                    if (loadDisabled) false
                    else { cache.addLast(AdItem(ad, now())); true }
                }
                if (!accepted) { ad.destroy(); return@forNativeAd }

                deferred.complete(Result.success(Unit))
                if (!loader.isLoading) finishLoad(deferred)   // 요청한 광고가 다 왔을 때
            }
            .withAdListener(object : AdListener() {

                override fun onAdImpression() {
                    super.onAdImpression()
                    Timber.d("ad_ 광고 노출 기록됨")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    Timber.d("ad_ 광고 클릭 감지")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Timber.w("ad_ 광고 로드 실패 $error")
                    deferred.complete(Result.failure(AppError.AdLoadError(error.message)))
                    finishLoad(deferred)
                }
            })
            .build()

        loader.loadAds(AdRequest.Builder().build(), adConfig.preloadCount)

        // 콜백이 끝내 오지 않는 경우 대비
        serviceScope.launch {
            delay(adConfig.loadTimeoutMs.milliseconds)
            deferred.complete(Result.failure(AppError.AdLoadError("load timeout")))
            finishLoad(deferred)
        }
    }

    private fun finishLoad(deferred: CompletableDeferred<Result<Unit>>) {
        synchronized(lock) { if (inFlight === deferred) inFlight = null }
    }

    // 만료되지 않은 광고 가져오기
    private fun ArrayDeque<AdItem>.pollValidAd(): AdItem? {
        val expired = mutableListOf<AdItem>()
        val valid = synchronized(lock) {
            var found: AdItem? = null
            while (isNotEmpty() && found == null) {
                val item = removeFirst()
                if (isAdExpired(item)) expired += item else found = item
            }
            found
        }
        expired.forEach { it.destroy() }
        return valid
    }

    private fun cacheSize() = synchronized(lock) { cache.size }

    private fun remainingRequestIntervalMs() = synchronized(lock) {
        adConfig.minRequestIntervalMs - (now() - lastRequestTime)
    }

    private fun isAdExpired(item: AdItem) = now() - item.createAt >= adConfig.adExpireMs

    private fun isSlotExpired(slot: Slot) = now() - slot.createAt >= adConfig.minRefreshIntervalMs

    private fun now() = SystemClock.elapsedRealtime()
}