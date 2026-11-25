package com.dhkim139.admin.wheretogo.core

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dhkim139.admin.wheretogo.feature.login.LoginScreen
import com.macbook14inch.feature.admin.AdminNavHost

object Routes {
    const val LOGIN = "login"
    const val ADMIN = "admin"
}

@Composable
fun AppNavGraph(
    isLoggedIn: Boolean,
    onSessionExpired: ()-> Unit,
    reportId: String?,
    mail: String?
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = if (isLoggedIn) Routes.ADMIN else Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.ADMIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.ADMIN) {
            AdminNavHost(
                adminEmail = { mail },
                initialReportId = reportId,
                onSessionExpired = {
                    onSessionExpired()
                    navController.toLogin()
                },
                onRequestSignOut = { navController.toLogin() },
                onExit = null,
            )
        }
    }
}

private fun NavHostController.toLogin() = navigate(Routes.LOGIN) {
    popUpTo(Routes.ADMIN) { inclusive = true }
    launchSingleTop = true
}