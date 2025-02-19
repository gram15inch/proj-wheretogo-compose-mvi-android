package com.dhkim139.wheretogo.feature

import android.content.Context
import androidx.test.core.app.ActivityScenario
import com.dhkim139.wheretogo.MainActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


suspend fun getContext(): Context {
    return suspendCancellableCoroutine { con ->
        ActivityScenario.launch(MainActivity::class.java).onActivity { activity ->
            con.resume(activity as Context)
        }
    }
}