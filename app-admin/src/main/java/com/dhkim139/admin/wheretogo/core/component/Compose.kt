package com.dhkim139.admin.wheretogo.core.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


suspend fun SnackbarHostState.showSnackbarBriefly(
    message: String,
    duration: Long = 700L
) {
    coroutineScope {
        val job = launch {
            showSnackbar(
                message = message,
                duration = SnackbarDuration.Indefinite
            )
        }
        delay(duration)
        job.cancel()
    }
}