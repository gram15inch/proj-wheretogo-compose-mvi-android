package com.wheretogo.presentation.model

import com.google.android.gms.ads.nativead.NativeAd

data class AdItem(val nativeAd: Any? = null, val createAt: Long) {
    fun destroy() {
        (nativeAd as? NativeAd)?.destroy()
    }
}