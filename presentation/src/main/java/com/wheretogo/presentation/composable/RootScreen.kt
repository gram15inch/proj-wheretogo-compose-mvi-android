package com.wheretogo.presentation.composable

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.R
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.getString
import com.wheretogo.presentation.feature.shortShow
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.viewmodel.RootViewModel

@Composable
fun RootScreen(viewModel: RootViewModel = hiltViewModel()) {
    WhereTogoTheme {
        val navController = rememberNavController()
        val state by viewModel.rootScreenState.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            EventBus.eventFlow.collect{
                val eventMsg = it.second
                when (it.first) {
                    AppEvent.SNACKBAR -> {
                        val msg = eventMsg.getString(context)
                        viewModel.snackbarHostState.shortShow(msg)
                    }

                    AppEvent.NAVIGATION -> {
                        val navigation = eventMsg.getString(context)
                        navController.navigate(navigation)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SnackbarHost(
                modifier = Modifier
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter)
                    .zIndex(99f),
                hostState = viewModel.snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData,
                        containerColor = Color.DarkGray,
                        contentColor = Color.White,
                        actionColor = Color.Cyan
                    )
                }
            )
            SlideAnimation(
                modifier = Modifier.zIndex(1f),
                visible = state.isRequestLogin,
                direction = AnimationDirection.CenterDown
            ) {
                LoginScreen()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val home = stringResource(R.string.navi_home)
                val drive = stringResource(R.string.navi_drive)
                val courseAdd = stringResource(R.string.navi_courseAdd)
                val setting = stringResource(R.string.navi_setting)

                NavHost(navController = navController, startDestination = home) {
                    composable(home) {
                        HomeScreen(navController)
                    }
                    composable(drive,
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        DriveScreen(navController)
                    }
                     composable(courseAdd,
                        enterTransition = { slideInVertically(initialOffsetY = { it }) },
                        exitTransition = { slideOutVertically(targetOffsetY = { it }) }
                    ) {
                         CourseAddScreen()
                     }
                    composable(setting,
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        SettingScreen(navController)
                    }
                }
            }
        }
    }
}