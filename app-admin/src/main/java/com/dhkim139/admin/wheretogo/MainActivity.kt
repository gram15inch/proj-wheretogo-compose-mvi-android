package com.dhkim139.admin.wheretogo

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim139.admin.wheretogo.core.AdminNotification.Companion.INTENT_REPORT_ID
import com.dhkim139.admin.wheretogo.core.AppNavGraph
import com.dhkim139.admin.wheretogo.core.NotificationPermissionHelper
import com.dhkim139.admin.wheretogo.core.theme.AdminTheme
import com.dhkim139.core.ui.event.AppEvent
import com.dhkim139.core.ui.event.AppEventSendEffect
import com.dhkim139.core.ui.event.EventBus
import com.dhkim139.core.ui.permission.AppPermission
import com.dhkim139.core.ui.permission.checkFalseOrData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        if(!BuildConfig.DEBUG)
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
                        when (it) {
                            is AppEvent.Permission -> {
                                multiplePermissionsLauncher.launch(it.permission.names.toTypedArray())
                            }
                            else -> {}
                        }
                    }
                }

                AppNavGraph(
                    isLoggedIn = uiState.isLoggedIn,
                    reportId = intent?.getStringExtra(INTENT_REPORT_ID),
                    mail = uiState.mail
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

}