package com.wheretogo.presentation.composable.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wheretogo.presentation.AppLifecycle

@Composable
fun LifecycleDisposer(
    onEventChange: (AppLifecycle) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        onEventChange(AppLifecycle.onLaunch)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> onEventChange(AppLifecycle.onResume)
                Lifecycle.Event.ON_PAUSE -> onEventChange(AppLifecycle.onPause)
                Lifecycle.Event.ON_DESTROY -> onEventChange(AppLifecycle.onDestory)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            onEventChange(AppLifecycle.onDispose)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}