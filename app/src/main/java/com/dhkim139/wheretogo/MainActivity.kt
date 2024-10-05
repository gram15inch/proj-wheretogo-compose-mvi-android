package com.dhkim139.wheretogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dhkim139.wheretogo.ui.composable.DriveContent
import com.dhkim139.wheretogo.ui.composable.HomeContent
import com.dhkim139.wheretogo.ui.theme.WhereTogoTheme
import com.dhkim139.wheretogo.ui.theme.White100
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhereTogoTheme {
                val navController = rememberNavController()
                Box(
                    modifier = Modifier
                        .background(Color.Gray)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(White100)
                    ) {

                        val displayMaxWidth = min(400.dp, maxWidth)
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") { HomeContent(displayMaxWidth, navController) }
                            composable("drive") { DriveContent(navController) }
                        }
                    }
                }
            }
        }
    }
}



