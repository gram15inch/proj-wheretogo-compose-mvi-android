package com.dhkim139.admin.wheretogo.core

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dhkim139.admin.wheretogo.feature.login.LoginScreen
import com.dhkim139.admin.wheretogo.feature.dashboard.DashboardScreen
import com.dhkim139.admin.wheretogo.feature.report.ReportDetailScreen
import com.dhkim139.admin.wheretogo.feature.report.ReportListScreen
import com.dhkim139.admin.wheretogo.feature.report.model.ModerateSeverity
import com.dhkim139.admin.wheretogo.feature.report.model.ReportStatus

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val REPORT_LIST =
        "report_list?reportStatus={reportStatus}&reportModerate={reportModerate}"
    const val REPORT_DETAIL = "report_detail/{reportId}"

    fun reportList(
        reportStatus: ReportStatus? = null,
        reportModerate: ModerateSeverity? = null,
    ): String {
        val status = reportStatus?.name ?: ""
        val moderate = reportModerate?.name ?: ""
        return "report_list?reportStatus=$status&reportModerate=$moderate"
    }

    fun reportDetail(reportId: String) = "report_detail/$reportId"
}

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToReportList = { status, moderate ->
                    navController.navigate(
                        Routes.reportList(
                            status,
                            moderate
                        )
                    )
                },
                onNavigateToReportDetail = { reportId ->
                    navController.navigate(Routes.reportDetail(reportId))
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.REPORT_LIST,
            arguments = listOf(
                navArgument("reportStatus") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                },
                navArgument("reportModerate") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                },
            ),
        ) {
            ReportListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { reportId ->
                    navController.navigate(Routes.reportDetail(reportId))
                },
            )
        }

        composable(
            route = Routes.REPORT_DETAIL,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            ReportDetailScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
