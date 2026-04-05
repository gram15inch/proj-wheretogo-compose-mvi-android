package com.dhkim139.admin.wheretogo.feature.main

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dhkim139.admin.wheretogo.core.AdminNavGraph
import com.dhkim139.admin.wheretogo.core.AdminNotification.Companion.INTENT_REPORT_ID
import com.dhkim139.admin.wheretogo.core.NotificationPermissionHelper
import com.dhkim139.admin.wheretogo.core.Routes
import com.dhkim139.admin.wheretogo.core.theme.AdminTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionHelper by lazy {
        NotificationPermissionHelper(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        // 화면 캡처 방지
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )

        // 알림 권한 요청
        notificationPermissionHelper.requestIfNeeded()

        setContent {
            AdminTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // 앱 인증 전 화면 로드 방지
                if (!uiState.isReady) return@AdminTheme

                val navController = rememberNavController()
                AdminNavGraph(
                    navController = navController,
                    startDestination = if (uiState.isLoggedIn) Routes.DASHBOARD else Routes.LOGIN,
                )

                // 알림에서 접근시 이동
                LaunchedEffect(intent) {
                    if (uiState.isLoggedIn)
                        handleNotificationIntent(intent, navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?, navController: NavController) {
        intent?.getStringExtra(INTENT_REPORT_ID)?.let { id ->
            navController.navigate(Routes.reportDetail(id))
        }
    }
}