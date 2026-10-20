package com.wheretogo.presentation.composable.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.presentation.composable.content.ad.LocalNativeAdView


@Composable
fun CardAdEffect(
    nativeAd: NativeAd?
) {
    val nav = LocalNativeAdView.current
    var isComposition by remember { mutableStateOf(false) }

    LaunchedEffect(nativeAd) {
        if (nativeAd != null)
            isComposition = true
    }

    if (isComposition) {
        DisposableEffect(nativeAd) {

            if (nativeAd != null) {
                nav?.setNativeAd(nativeAd)
            }
            onDispose {
                isComposition = false
            }
        }
    }

}


@Composable
fun RowAdEffect(nativeAd: NativeAd?) {
    val nav = LocalNativeAdView.current
    var isComposition by remember { mutableStateOf(false) }

    LaunchedEffect(nativeAd) {
        if (nativeAd != null)
            isComposition = true
    }

    if (isComposition) {
        DisposableEffect(nativeAd) {
            if (nativeAd != null) {
                nav?.setNativeAd(nativeAd)
            }
            onDispose {
                isComposition = false
            }
        }
    }

}
