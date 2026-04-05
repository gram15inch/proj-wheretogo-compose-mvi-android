package com.dhkim139.admin.wheretogo.feature.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim139.admin.wheretogo.feature.report.model.ModerateSeverity
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportStatus
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun ReportListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ReportListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    ReportListContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = onNavigateToDetail,
        onSelectStatus = viewModel::selectStatus,
        onSelectModerate = viewModel::selectModerate,
        onRefresh = viewModel::refresh,
        onLoadNextPage = viewModel::loadNextPage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportListContent(
    uiState: ReportListUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onSelectStatus: (ReportStatus?) -> Unit,
    onSelectModerate: (ModerateSeverity?) -> Unit,
    onRefresh: () -> Unit,
    onLoadNextPage: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("신고 목록") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // 상태 필터
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    ReportStatusChip(
                        status = null,
                        label = "전체",
                        selected = uiState.selectedStatus == null,
                        onClick = { onSelectStatus(null) },
                    )
                }
                items(ReportStatus.entries.filter { it != ReportStatus.UNKNOWN }) { status ->
                    ReportStatusChip(
                        status = status,
                        label = status.toDisplayName(),
                        selected = uiState.selectedStatus == status,
                        onClick = { onSelectStatus(status) },
                    )
                }
            }

            // 심각도 필터
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    ReportStatusChip(
                        status = null,
                        label = "전체",
                        selected = uiState.selectedModerate == null,
                        onClick = { onSelectModerate(null) },
                    )
                }
                items(ModerateSeverity.entries.filter { it != ModerateSeverity.UNKNOWN }) { moderate ->
                    ReportStatusChip(
                        status = null,
                        label = moderate.toDisplayName(),
                        selected = uiState.selectedModerate == moderate,
                        onClick = { onSelectModerate(moderate) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 신고 목록
            PullToRefreshBox(
                isRefreshing = uiState.isLoading && uiState.reports.isEmpty(),
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                ReportLazyList(
                    reports = uiState.reports,
                    hasNext = uiState.hasNext,
                    isLoadingMore = uiState.isLoading && uiState.reports.isNotEmpty(),
                    onItemClick = onNavigateToDetail,
                    onLoadNextPage = onLoadNextPage,
                )
            }
        }
    }
}

@Composable
private fun ReportLazyList(
    reports: List<Report>,
    hasNext: Boolean,
    isLoadingMore: Boolean,
    onItemClick: (String) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { info ->
                val total = info.totalItemsCount
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                total > 0 && last >= total - 3
            }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd) onLoadNextPage()
            }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (reports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("신고 내역이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(reports, key = { it.id }) { report ->
            ReportSummaryCard(
                report = report,
                onClick = { onItemClick(report.id) },
            )
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
        }
    }
}


// 미리보기

private val previewReports = (1..5).map { i ->
    Report(
        id = "$i", contentId = "c$i", contentGroupId = null,
        type = listOf("욕설", "스팸", "불법", "혐오")[i % 4],
        reason = "신고 사유 $i 번입니다.",
        status = ReportStatus.PENDING,
        targetUserId = "u$i", targetUserName = "사용자$i",
        userId = "reporter$i",
        moderate = ModerateSeverity.entries[i % 4],
        createAt = System.currentTimeMillis() - i * 600_000L,
    )
}

@Preview(showBackground = true, name = "신고 목록 - 데이터 있음")
@Composable
private fun ReportListPreview() {
    MaterialTheme {
        ReportListContent(
            uiState = ReportListUiState(reports = previewReports),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onNavigateToDetail = {},
            onSelectStatus = {},
            onSelectModerate = {},
            onRefresh = {},
            onLoadNextPage = {},
        )
    }
}
