package com.wheretogo.presentation.composable

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.theme.White100
import com.wheretogo.presentation.viewmodel.RootViewModel

@Composable
fun RootScreen(viewModel: RootViewModel = hiltViewModel()) {
    WhereTogoTheme {
        val navController = rememberNavController()
        val state by viewModel.rootScreenState.collectAsState()

        Box(
            modifier = Modifier
                .background(Color.Gray)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SlideAnimation(
                modifier = Modifier.zIndex(1f),
                visible = state.isRequestLogin,
                direction = AnimationDirection.CenterDown
            ) {
                LoginScreen()
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(White100)
            ) {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController) }
                    composable("drive",
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) { DriveScreen(navController) }
                    composable("bookmark") { BookmarkScreen(navController) }
                    composable("courseAdd",
                        enterTransition = { slideInVertically(initialOffsetY = { it }) },
                        exitTransition = { slideOutVertically(targetOffsetY = { it }) }
                    ) { CourseAddScreen(navController) }
                    composable("setting",
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) { SettingScreen(navController) }
                }
            }
        }
    }
}