package com.wheretogo.presentation.composable

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheretogo.presentation.AppEvent
import com.wheretogo.presentation.AppPermission
import com.wheretogo.presentation.AppScreen
import com.wheretogo.presentation.composable.content.AnimationDirection
import com.wheretogo.presentation.composable.content.SlideAnimation
import com.wheretogo.presentation.composable.effect.AppEventReceiveEffect
import com.wheretogo.presentation.composable.effect.AppEventSendEffect
import com.wheretogo.presentation.feature.EventBus
import com.wheretogo.presentation.feature.checkFalseOrData
import com.wheretogo.presentation.feature.openUri
import com.wheretogo.presentation.feature.show
import com.wheretogo.presentation.composable.gallery.GalleryFlow
import com.wheretogo.presentation.theme.Palette
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
        val multiplePermissionsLauncher
        = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            coroutine.launch {
                val permission = AppPermission.valueOf(permissions.keys.toSet())
                val result = checkFalseOrData(context, permission)
                EventBus.resultPermission(AppEvent.Permission(permission), result)
            }
        }

        AppEventSendEffect {
            coroutine.launch {
                viewModel.eventSend(it)
                when (it) {
                    is AppEvent.SnackBar -> {
                        viewModel.snackbarHostState.show(context, it.msg) { uri ->
                            openUri(context, uri)
                        }
                    }

                    is AppEvent.Navigation -> {
                        val from  = it.from?.toString()
                        val to = it.to.toString()
                        navController.navigate(to){
                            if(from == null)
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = it.inclusive }
                            else
                                popUpTo(from) { inclusive = it.inclusive }
                        }
                    }

                    is AppEvent.Permission -> {
                        multiplePermissionsLauncher.launch(it.permission.names.toTypedArray())
                    }
                    is AppEvent.SignInScreen -> {
                        viewModel.setSignInScreenVisible(true)
                    }
                }
            }
        }

        AppEventReceiveEffect {
            viewModel.eventReceive(it)
            when(it.event){
                is AppEvent.SignInScreen ->{
                    viewModel.setSignInScreenVisible(false)
                }
                else -> {}
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
                        contentColor = Palette.White,
                        actionColor = Color.Cyan
                    )
                }
            )

            SlideAnimation(
                modifier = Modifier.zIndex(1f),
                visible = state.isSignInScreenVisible,
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
                val gallery = AppScreen.Gallery.toString()
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
                    composable(gallery,
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        GalleryFlow()
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