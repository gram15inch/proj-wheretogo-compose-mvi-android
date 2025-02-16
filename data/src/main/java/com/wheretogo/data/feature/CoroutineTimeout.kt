package com.wheretogo.data.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun Job.timeout(second:Int){
    CoroutineScope(Dispatchers.IO).launch {
        delay(second*1000L)
        this@timeout.cancel()
    }
}