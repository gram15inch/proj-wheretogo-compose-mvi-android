package com.wheretogo.presentation.composable.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.presentation.composable.content.ad.LocalNativeAdView


@Composable
fun CardAdEffect(nativeAd: NativeAd?, isCompact: Boolean, tiltLines:Int, onMaxLineUpdate: (Int)-> Unit){
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val nav = LocalNativeAdView.current
    if (isCompact) {
        LaunchedEffect(tiltLines, fontScale) {
            if (tiltLines != -1) {
                val maxLine= when {
                    fontScale >= 1.1f && tiltLines >= 2 -> 0
                    tiltLines > 1 -> 1
                    else -> {
                        2
                    }
                }
                onMaxLineUpdate(maxLine)
                if (nativeAd != null)
                    nav?.setNativeAd(nativeAd)
            }
        }
    } else {
        LaunchedEffect(nativeAd) {
            if (nativeAd != null)
                nav?.setNativeAd(nativeAd)
        }
    }
}
