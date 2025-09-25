package com.wheretogo.presentation.composable.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.ads.nativead.NativeAd
import com.wheretogo.presentation.composable.content.ad.LocalNativeAdView


@Composable
fun CardAdEffect(
    nativeAd: NativeAd?,
    isCompact: Boolean,
    tiltLines: Int,
    onMaxLineUpdate: (Int) -> Unit
) {
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val nav = LocalNativeAdView.current
    var isComposition by remember { mutableStateOf(false) }
    if (isCompact) {
        LaunchedEffect(tiltLines, fontScale) {
            if (tiltLines != -1) {
                val maxLine = when {
                    fontScale >= 1.1f && tiltLines >= 2 -> 0
                    tiltLines > 1 -> 1
                    else -> {
                        2
                    }
                }
                onMaxLineUpdate(maxLine)
            }
        }
    }

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
                nativeAd?.destroy()
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
                nativeAd?.destroy()
                isComposition = false
            }
        }
    }

}
