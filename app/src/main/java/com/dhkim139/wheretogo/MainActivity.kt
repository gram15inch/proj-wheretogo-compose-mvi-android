package com.dhkim139.wheretogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dhkim139.permission.locationRequest
import com.wheretogo.presentation.composable.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
           MainScreen()

        }
        //CoroutineScope(Dispatchers.IO).launch { createDummy(this@MainActivity) }
        locationRequest{ }
    }
}



