package com.wheretogo.presentation.composable.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun rememberDelayedState(
    isLoading: Boolean,
    delayMillis: Long = 300L
): Boolean {
    var delayedLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(delayMillis)
            delayedLoading = true
        } else {
            delayedLoading = false
        }
    }

    return delayedLoading
}