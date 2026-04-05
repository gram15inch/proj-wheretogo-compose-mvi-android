package com.dhkim139.admin.wheretogo.feature.report

import android.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim139.admin.wheretogo.core.component.showSnackbarBriefly
import com.dhkim139.admin.wheretogo.core.theme.AdminTheme
import com.dhkim139.admin.wheretogo.feature.report.model.ModerateSeverity
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportContent
import com.dhkim139.admin.wheretogo.feature.report.model.ReportInput
import com.dhkim139.admin.wheretogo.feature.report.model.ReportStatus
import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.comment.Comment
import com.wheretogo.domain.model.course.Course
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.processSuccess) {
        if (uiState.processSuccess) {
            snackbarHostState.showSnackbarBriefly("처리가 완료되었습니다.", duration= 1000)
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    ReportDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onRequestAction = viewModel::requestAction,
        onConfirmAction = viewModel::confirmAction,
        onCancelAction = viewModel::cancelAction,
        onRetry = viewModel::loadReport,
    )
}

@Composable
private fun ReportDetailContent(
    uiState: ReportDetailUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onRequestAction: (ActionButtonType) -> Unit,
    onConfirmAction: (ReportInput) -> Unit,
    onCancelAction: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.report == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("불러오기 실패", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onRetry) { Text("다시 시도") }
                    }
                }

                else -> {
                    ReportDetailBody(
                        report = uiState.report,
                        content = uiState.content,
                        isProcessing = uiState.isProcessing,
                        onNavigateBack = onNavigateBack,
                        onRequestAction = onRequestAction,
                    )
                }
            }
        }
    }

    // 바텀시트
    if (uiState.actionState != null) {
        ActionBottomSheet(onDismiss = onCancelAction) {
            ActionContent(
                actionState = uiState.actionState,
                onConfirm = onConfirmAction,
                onDismiss = onCancelAction
            )
        }
    }
}

@Composable
private fun ReportDetailBody(
    report: Report,
    content: Any?,
    isProcessing: Boolean,
    onNavigateBack: () -> Unit,
    onRequestAction: (ActionButtonType) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // 뒤로가기 상단 버튼
            BackTopButton(onNavigateBack)

            // 신고 개요
            IntroSection(report)

            SectionDivider()

            // 컨텐츠 정보
            InfoSection(label = "내용") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    when (content) {
                        is Course -> CourseDetail(content)
                        is CheckPoint -> CheckPointDetail(content)
                        is Comment -> CommentDetail(content)
                        else -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }

            // 신고 이유
            InfoSection(label = "신고 사유") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                ) {
                    Text(
                        text = report.reason,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            ThinDivider()

            // 작성자
            InfoSection(label = "작성자") {
                InfoRow(label = "이름", value = report.targetUserName)
                InfoRow(label = "사용자 ID", value = report.targetUserId, mono = true)
            }

            Spacer(Modifier.height(24.dp))
            ThinDivider()

            // 신고자
            InfoSection(label = "신고자") {
                InfoRow(label = "사용자 ID", value = report.userId, mono = true, isLast = true)
            }

            Spacer(Modifier.height(24.dp))
        }

        // 액션 버튼 하단바
        if (report.status == ReportStatus.PENDING) {
            ActionBottomBar(
                isProcessing = isProcessing,
                onRequestAction = onRequestAction,
            )
        }
    }
}

@Composable
fun BackTopButton(onNavigateBack: () -> Unit){
    Box(
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
    ) {
        Surface(
            onClick = onNavigateBack,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}


@Composable
fun IntroSection(report: Report){
    Column(
        modifier = Modifier.padding(
            start = 24.dp, end = 24.dp,
            top = 4.dp, bottom = 28.dp,
        ),
    ) {
        SeverityChip(severity = report.moderate)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${report.type} · ${report.targetUserName}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 30.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = SimpleDateFormat(
                "yyyy.MM.dd HH:mm 접수",
                Locale.KOREA,
            ).format(Date(report.createAt)),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.outline,
        )
    }

    if (report.status != ReportStatus.PENDING) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
        ) {
            Text(
                text = "이미 처리된 신고입니다 (${report.status.raw})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ActionBottomBar(
    isProcessing: Boolean,
    onRequestAction: (ActionButtonType) -> Unit,
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 10.dp)
        ) {
            // 사용자 차단
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionButton(
                    label = "작성자 차단",
                    subLabel = "컨텐츠 숨기기",
                    containerColor = Color(0xFFFCEBEB),
                    contentColor = Color(0xFFA32D2D),
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    onClick = { onRequestAction(ActionButtonType.BLOCK_WRITER_AND_HIDE_CONTENT) },
                )
                ActionButton(
                    label = "컨텐츠 숨기기",
                    subLabel = "",
                    containerColor = Color(0xFFFCEBEB),
                    contentColor = Color(0xFFA32D2D),
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    onClick = { onRequestAction(ActionButtonType.HIDE_CONTENT) },
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 작성자 차단
                ActionButton(
                    label = "신고자 차단",
                    subLabel = "컨텐츠 보이기",
                    containerColor = Color(0xFFFAEEDA),
                    contentColor = Color(0xFF633806),
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    onClick = { onRequestAction(ActionButtonType.BLOCK_REPORTER_AND_REJECT) },
                )

                ActionButton(
                    label = "컨텐츠 보이기",
                    subLabel = "",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    onClick = { onRequestAction(ActionButtonType.REJECT) },
                )
            }

        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    subLabel: String,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        enabled = enabled,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 13.dp, horizontal = 10.dp),
        ) {
            Text(
                modifier = Modifier.padding(0.dp),
                text = label,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Text(
                modifier = Modifier.padding(0.dp),
                text = subLabel,
                fontSize = 8.sp,
                lineHeight = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        content()
    }
}

@Composable
fun ActionTitle(label: String) {
    Text(
        text = label,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "처리 사유를 입력해주세요. 이 작업은 되돌릴 수 없습니다.",
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun ActionContent(
    actionState: ActionState,
    onConfirm: (ReportInput) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val showDuration = listOf(ActionButtonType.BLOCK_WRITER_AND_HIDE_CONTENT, ActionButtonType.BLOCK_REPORTER_AND_REJECT).contains(actionState.type)
    val presetDurations = listOf(1, 3, 7, 30)
    val durationGridMaxRow = 4

    // 차단 기간 버튼 상태
    var selectedDuration by remember(actionState.type) { mutableIntStateOf(1) }

    // 직접 입력 모드
    var isCustom by remember(actionState.type) { mutableStateOf(false) }
    var reasonInput :String? by remember(actionState.type) { mutableStateOf(null) }
    var customDayInput by remember(actionState.type) { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if(showDuration)
            reasonInput = ""
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
            .navigationBarsPadding(),
    ) {
        ActionTitle(actionState.type.label)

        // 차단 기간 선택
        AnimatedVisibility(
            visible = showDuration,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "차단 기간",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp),
                )

                // 프리셋 버튼 그리드
                val rows = presetDurations.chunked(durationGridMaxRow)
                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        row.forEach { duration ->
                            DurationChip(
                                label = "${duration}일",
                                selected = !isCustom && selectedDuration == duration,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedDuration = duration
                                    isCustom = false
                                },
                            )
                        }
                        // 마지막 행이 4개 미만이면 남은 공간 채우기
                        repeat(durationGridMaxRow - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // 직접 입력 버튼 (전체 너비)
                DurationChip(
                    label = if (isCustom && customDayInput.isNotBlank()) "${customDayInput}일" else "직접 입력",
                    selected = isCustom,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { isCustom = true },
                )

                // 직접 입력 필드 — 직접 입력 선택 시에만 표시
                AnimatedVisibility(visible = isCustom) {
                    OutlinedTextField(
                        value = customDayInput,
                        onValueChange = { v ->
                            if (v.all { it.isDigit() } && v.length <= 4) {
                                customDayInput = v
                                v.toIntOrNull()?.let { days ->
                                    if (days > 0) selectedDuration = days
                                }
                            }
                        },
                        label = { Text("차단 일수") },
                        suffix = { Text("일") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 사유 입력
        OutlinedTextField(
            value = reasonInput?:"",
            onValueChange = { reasonInput = it },
            placeholder = { Text("처리 사유 (${if(showDuration)"필수" else "선택"})", color = MaterialTheme.colorScheme.outline) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(Modifier.height(16.dp))

        // 확인 버튼
        val canConfirm =
            if (showDuration) selectedDuration > 0 && reasonInput?.isNotBlank() ?: true else true

        actionState.type
        Button(
            onClick = {
                onConfirm(
                    ReportInput(
                        reason = reasonInput,
                        duration = if (showDuration) selectedDuration else null,
                    )
                )
            },
            enabled = canConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(
                text = "확인",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(8.dp))

        // 취소 버튼
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "취소",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DurationChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (selected) Color(0xFFE6F1FB) else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (selected) Color(0xFF378ADD) else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (selected) Color(0xFF0C447C) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
        )
    }
}


@Composable
private fun CourseDetail(course: Course) {
    ContentCard(
        content = ReportContent(
            text = course.courseName,
            isHide = course.isHide,
            reportedCount = course.reportedCount,
            likeCount = course.like,
            createAt = course.createAt,
            updateAt = course.updateAt,
        ),
    )
}

@Composable
private fun CheckPointDetail(checkpoint: CheckPoint) {
    ContentCard(
        content = ReportContent(
            text = checkpoint.caption,
            subText = checkpoint.description,
            imgUrl = checkpoint.thumbnail,
            isHide = checkpoint.isHide,
            reportedCount = checkpoint.reportedCount,
            likeCount = 0,
            createAt = checkpoint.createAt,
            updateAt = checkpoint.updateAt,
        ),
    )
}

@Composable
private fun CommentDetail(comment: Comment) {
    ContentCard(
        content = ReportContent(
            text = comment.oneLineReview,
            subText = comment.detailedReview,
            isHide = comment.isHide,
            reportedCount = comment.reportedCount,
            likeCount = comment.like,
            createAt = comment.createAt,
            updateAt = comment.updateAt,
        ),
    )
}

@Composable
private fun SeverityChip(severity: ModerateSeverity) {
    val (text, bg, fg) = when (severity) {
        ModerateSeverity.HIGH -> Triple("위험", Color(0xFFFCEBEB), Color(0xFFA32D2D))
        ModerateSeverity.MEDIUM -> Triple("주의", Color(0xFFFAEEDA), Color(0xFF633806))
        ModerateSeverity.LOW -> Triple("낮음", Color(0xFFEAF3DE), Color(0xFF27500A))
        ModerateSeverity.SAFE -> Triple("안전", Color(0xFFEAF3DE), Color(0xFF27500A))
        ModerateSeverity.UNKNOWN -> Triple("알 수 없음", Color(0xFFF1EFE8), Color(0xFF444441))
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg),
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = fg,
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun InfoSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 0.3.sp,
            modifier = Modifier.padding(bottom = 14.dp),
        )
        content()
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    mono: Boolean = false,
    isLast: Boolean = false,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = value,
                fontSize = if (mono) 13.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}


// 미리보기

private val previewReport = Report(
    id = "r1", contentId = "cnt_xyz789", contentGroupId = "grp_abc123",
    type = "욕설",
    reason = "심한 욕설과 혐오 발언으로 다른 이용자에게 심각한 불쾌감을 주었습니다. 반복적인 패턴이 확인되었습니다.",
    status = ReportStatus.PENDING,
    targetUserId = "u_a1b2c3d4", targetUserName = "홍길동",
    userId = "u_reporter56",
    moderate = ModerateSeverity.HIGH,
    createAt = System.currentTimeMillis() - 3_600_000,
)

private val previewContent = Comment(
    oneLineReview = "여기 진짜 별로다",
    detailedReview = "다신 안갑니다. 진짜 별로에요 이게뭔가요.",
    isHide = true,
    reportedCount = 3,
    like = 1,
    updateAt = System.currentTimeMillis(),
    createAt = System.currentTimeMillis() - 3_600_000
)

@Preview(showBackground = true, name = "신고 상세 — 토스 스타일")
@Composable
private fun ReportDetailPreview() {
    AdminTheme {
        ReportDetailContent(
            uiState = ReportDetailUiState(report = previewReport, content = previewContent),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onRequestAction = {},
            onConfirmAction = {},
            onCancelAction = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "신고 상세 — 처리 완료")
@Composable
private fun ReportDetailApprovedPreview() {
    AdminTheme {
        ReportDetailContent(
            uiState = ReportDetailUiState(
                report = previewReport.copy(status = ReportStatus.APPROVED),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onRequestAction = {},
            onConfirmAction = {},
            onCancelAction = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, name = "콘텐츠 삭제 — 기간 선택 없음")
@Composable
private fun UserActionDeleteContentPreview() {
    AdminTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
        ){
            ActionContent(
                actionState = ActionState(
                    type = ActionButtonType.HIDE_CONTENT
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "신고자 차단 — 기간 선택 있음")
@Composable
private fun UserActionBlockReporterPreview() {
    AdminTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
        ) {
            ActionContent(
                actionState = ActionState(
                    type = ActionButtonType.BLOCK_REPORTER_AND_REJECT
                ),
            )
        }
    }
}
