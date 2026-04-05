package com.dhkim139.admin.wheretogo.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim139.admin.wheretogo.feature.report.ReportSummaryCard
import com.dhkim139.admin.wheretogo.feature.report.model.ModerateSeverity
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportStatus

@Composable
fun DashboardScreen(
    onNavigateToReportList: (ReportStatus, ModerateSeverity?) -> Unit,
    onNavigateToReportDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            when(it.trim()){
                "HTTP 403" -> {
                    snackbarHostState.showSnackbar("관리자 계정이 필요합니다.")
                    onNavigateToLogin()
                }
                else -> {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearError()
                }
            }
        }
    }

    DashboardContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::load,
        onLogout = viewModel::logout,
        onNavigateToReportList = onNavigateToReportList,
        onNavigateToReportDetail = onNavigateToReportDetail,
        onNavigateToLogin = onNavigateToLogin,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToReportList: (ReportStatus, ModerateSeverity?) -> Unit,
    onNavigateToReportDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text("대시보드")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 대시보드
            uiState.stats?.let { stats ->
                StatsGrid(stats = stats, onNavigateToReportList = onNavigateToReportList)
            } ?: Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
            }


            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "최근 미처리 신고",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Text(
                    text = "전체 보기",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        onNavigateToReportList(
                            ReportStatus.PENDING,
                            null
                        )
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            // 최근 미처리 목록
            if (uiState.recentReports.isEmpty() && !uiState.isLoading) {
                Text(
                    text = "미처리 신고가 없습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                uiState.recentReports.forEach { report ->
                    ReportSummaryCard(
                        report = report,
                        onClick = { onNavigateToReportDetail(report.id) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        Box(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxSize()
                .systemBarsPadding()
                .padding(start = 20.dp, bottom = 8.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                modifier = Modifier.clickable {
                    onLogout()
                    onNavigateToLogin()
                },
                text = uiState.email, fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun StatsGrid(
    stats: DashboardStatsDto,
    onNavigateToReportList: (ReportStatus, ModerateSeverity?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            label = "미처리",
            value = stats.pendingCount.toString(),
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onNavigateToReportList(ReportStatus.PENDING, null)
                },
            valueColor = MaterialTheme.colorScheme.error,
        )
        StatCard(
            label = "오늘 차단",
            value = stats.approvedToday.toString(),
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onNavigateToReportList(ReportStatus.APPROVED, null)
                },
            valueColor = MaterialTheme.colorScheme.primary,
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            label = "위험 (High)",
            value = stats.highCount.toString(),
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onNavigateToReportList(ReportStatus.PENDING, ModerateSeverity.HIGH)
                },
            valueColor = MaterialTheme.colorScheme.error,
        )
        StatCard(
            label = "주의 (Medium)",
            value = stats.mediumCount.toString(),
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onNavigateToReportList(ReportStatus.PENDING, ModerateSeverity.MEDIUM)
                },
            valueColor = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// 미리보기

private val previewStats = DashboardStatsDto(
    pendingCount = 12,
    approvedToday = 5,
    highCount = 3,
    mediumCount = 7,
)

private val previewReports = listOf(
    Report(
        id = "1",
        contentId = "c1",
        contentGroupId = null,
        type = "욕설",
        reason = "심한 욕설을 사용했습니다",
        status = ReportStatus.PENDING,
        targetUserId = "u1",
        targetUserName = "홍길동",
        userId = "reporter1",
        moderate = ModerateSeverity.HIGH,
        createAt = System.currentTimeMillis() - 3600_000,
    ),
    Report(
        id = "2",
        contentId = "c2",
        contentGroupId = null,
        type = "스팸",
        reason = "광고성 컨텐츠",
        status = ReportStatus.PENDING,
        targetUserId = "u2",
        targetUserName = "이순신",
        userId = "reporter2",
        moderate = ModerateSeverity.MEDIUM,
        createAt = System.currentTimeMillis() - 7200_000,
    ),
)

@Preview(showBackground = true, name = "대시보드 - 데이터 있음")
@Composable
private fun DashboardPreview() {
    MaterialTheme {
        DashboardContent(
            uiState = DashboardUiState(
                email = "wheretogo@gmail.com",
                stats = previewStats,
                recentReports = previewReports,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onLogout = {},
            onNavigateToReportList = { _, _ -> },
            onNavigateToReportDetail = {},
        )
    }
}

@Preview(showBackground = true, name = "대시보드 - 빈 상태")
@Composable
private fun DashboardEmptyPreview() {
    MaterialTheme {
        DashboardContent(
            uiState = DashboardUiState(stats = previewStats),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onLogout = {},
            onNavigateToReportList = { _, _ -> },
            onNavigateToReportDetail = {},
        )
    }
}
