package com.wheretogo.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.theme.White100

@Composable
fun MainScreen() {
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
                NavHost(navController = navController, startDestination = "drive") {
                    composable("home") { HomeContent(displayMaxWidth, navController) }
                    composable("drive") { DriveContent(navController) }
                }
            }
        }
    }
}