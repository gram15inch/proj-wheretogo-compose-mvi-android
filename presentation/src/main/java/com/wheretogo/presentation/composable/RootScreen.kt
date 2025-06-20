package com.wheretogo.presentation.composable

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppScreen
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.openUri
import com.wheretogo.presentation.feature.shortShow
import com.wheretogo.presentation.theme.WhereTogoTheme
import com.wheretogo.presentation.viewmodel.RootViewModel
import kotlinx.coroutines.launch

@Composable
fun RootScreen(viewModel: RootViewModel = hiltViewModel()) {
    WhereTogoTheme {
        val navController = rememberNavController()
        val state by viewModel.rootScreenState.collectAsState()
        val context = LocalContext.current
        val coroutine = rememberCoroutineScope()
        val multiplePermissionsLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            coroutine.launch {
                permissions.forEach { (permission, bool) ->
                    EventBus.permissionResult(permission, bool)
                }
            }
        }

        LaunchedEffect(Unit) {
            EventBus.eventFlow.collect {
                coroutine.launch {
                    when (it) {
                        is AppEvent.SnackBar -> {
                            viewModel.snackbarHostState.shortShow(context, it.msg) { uri ->
                                openUri(context, uri)
                            }
                        }

                        is AppEvent.Navigation -> {
                            navController.navigate(it.destination)
                        }

                        is AppEvent.Permission -> {
                            val isDenied = ContextCompat
                                .checkSelfPermission(
                                    context,
                                    it.permission.name
                                ) == PackageManager.PERMISSION_DENIED
                            if (isDenied)
                                multiplePermissionsLauncher.launch(arrayOf(it.permission.name))
                        }
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
                val home = AppScreen.Home.toString()
                val drive = AppScreen.Drive.toString()
                val courseAdd = AppScreen.CourseAdd.toString()
                val setting = AppScreen.Setting.toString()

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