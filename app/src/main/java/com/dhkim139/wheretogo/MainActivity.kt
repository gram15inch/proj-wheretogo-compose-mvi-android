package com.dhkim139.wheretogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dhkim139.permission.locationRequest
import com.wheretogo.presentation.composable.RootScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RootScreen()
        }
        //CoroutineScope(Dispatchers.Default).launch { createDummy(this@MainActivity) }
        locationRequest { }
    }
}


