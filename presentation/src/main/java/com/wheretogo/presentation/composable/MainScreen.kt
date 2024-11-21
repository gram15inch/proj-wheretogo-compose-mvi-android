package com.wheretogo.presentation.composable

import android.util.Log
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheretogo.presentation.composable.test.DragTestScreen
import com.wheretogo.presentation.composable.test.ImageTestScreen
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.theme.White100
import com.wheretogo.presentation.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    WhereTogoTheme {
        val navController = rememberNavController()
        val state by viewModel.mainScreenState.collectAsState()

        LaunchedEffect(state.isRequestLogin) {
            if (state.isRequestLogin) {
                Log.d("tst4", "${state.isRequestLogin}")
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }


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
                    composable("home") { HomeScreen(displayMaxWidth, navController) }
                    composable("drive") { DriveScreen(navController) }
                    composable("login",
                        enterTransition = { slideInVertically(initialOffsetY = { it }) },
                        exitTransition = { slideOutVertically(targetOffsetY = { it }) })
                    { LoginScreen(navController) }
                    composable("test") { ImageTestScreen() }
                    composable("drag") { DragTestScreen() }
                }
            }
        }
    }
}