package com.dhkim139.admin.wheretogo.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dhkim139.admin.wheretogo.core.theme.AdminTheme
import com.dhkim139.admin.wheretogo.feature.report.model.ModerateSeverity
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportContent
import com.dhkim139.admin.wheretogo.feature.report.model.ReportStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportSummaryCard(
    report: Report,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 심각도 뱃지
            ModerateBadge(severity = report.moderate)

            Spacer(modifier = Modifier.width(12.dp))

            // 신고 내용
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = report.type,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "·",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = report.targetUserName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = report.reason,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 접수 시각
            Text(
                text = report.createAt.toRelativeTime(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
fun ModerateBadge(
    severity: ModerateSeverity,
    modifier: Modifier = Modifier,
) {
    val (text, container, content) = when (severity) {
        ModerateSeverity.HIGH -> Triple("위험", Color(0xFFFFE0E0), Color(0xFFB71C1C))
        ModerateSeverity.MEDIUM -> Triple("주의", Color(0xFFFFF3E0), Color(0xFFE65100))
        ModerateSeverity.LOW -> Triple("낮음", Color(0xFFFFFDE7), Color(0xFFF9A825))
        ModerateSeverity.SAFE -> Triple("안전", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        ModerateSeverity.UNKNOWN -> Triple("?", Color(0xFFF5F5F5), Color(0xFF757575))
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = container,
        modifier = modifier,
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = content,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun ReportStatusChip(
    status: ReportStatus?,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}


@Composable
fun ContentCard(
    content: ReportContent,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.background,
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {

            // 숨김 배너
            if (content.isHide) {
                HideBanner()
            }

            // 이미지
            if (content.imgUrl != null) {
                ContentImage(imgUrl = content.imgUrl)
            }

            // 본문
            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 14.dp,
                ),
            ) {
                Text(
                    text = content.text,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (content.subText != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = content.subText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 푸터
            ContentFooter(content = content)
        }
    }
}

@Composable
private fun ContentImage(imgUrl: String) {
    var revealed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = imgUrl,
            contentDescription = "콘텐츠 이미지",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .then(if (!revealed) Modifier.blur(20.dp) else Modifier),
        )

        // 블러 상태일 때만 클릭 안내 오버레이 표시
        if (!revealed) {
            Surface(
                onClick = { revealed = true },
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.45f),
            ) {
                Text(
                    text = "탭하여 이미지 보기",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun HideBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAEEDA))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFBA7517)),
        )
        Text(
            text = "숨김 처리된 콘텐츠",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF633806),
        )
    }
}

@Composable
private fun ContentFooter(content: ReportContent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatChip(
            icon = Icons.Outlined.Warning,
            label = "신고 ${content.reportedCount}",
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.width(12.dp))
        StatChip(
            icon = Icons.Outlined.FavoriteBorder,
            label = "좋아요 ${content.likeCount}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            DateText(prefix = "작성", timestamp = content.createAt)
            if (content.updateAt > content.createAt) {
                Spacer(Modifier.height(2.dp))
                DateText(prefix = "수정", timestamp = content.updateAt)
            }
        }
    }
}

@Composable
private fun StatChip(icon: ImageVector, label: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Text(text = label, fontSize = 12.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DateText(prefix: String, timestamp: Long) {
    Text(
        text = "$prefix ${SimpleDateFormat("MM/dd HH:mm", Locale.KOREA).format(Date(timestamp))}",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.outline,
    )
}



// 미리보기

private val now = System.currentTimeMillis()

@Preview(showBackground = true, name = "텍스트만")
@Composable
private fun ContentCardTextOnlyPreview() {
    AdminTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ContentCard(
                content = ReportContent(
                    text = "이 커뮤니티 진짜 별로네요 운영도 엉망이고 사람들도 너무해요",
                    isHide = false,
                    reportedCount = 3,
                    likeCount = 12,
                    createAt = now - 3_600_000,
                    updateAt = now - 3_600_000,
                ),
            )
        }
    }
}

@Preview(showBackground = true, name = "이미지 + subText + 숨김 (블러)")
@Composable
private fun ContentCardFullPreview() {
    AdminTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ContentCard(
                content = ReportContent(
                    text = "오늘 찍은 사진인데 어떻게 생각하세요?",
                    subText = "강남구 어딘가에서 찍었어요. 날씨가 너무 좋아서 나왔다가 우연히 발견한 골목입니다.",
                    imgUrl = "https://picsum.photos/seed/report/800/450",
                    isHide = true,
                    reportedCount = 7,
                    likeCount = 45,
                    createAt = now - 86_400_000,
                    updateAt = now - 3_600_000,
                ),
            )
        }
    }
}


private fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000 -> "방금"
        diff < 3_600_000 -> "${diff / 60_000}분 전"
        diff < 86_400_000 -> "${diff / 3_600_000}시간 전"
        else -> SimpleDateFormat("MM/dd", Locale.KOREA).format(Date(this))
    }
}


@Preview(showBackground = true)
@Composable
private fun ReportSummaryCardPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ReportSummaryCard(
                report = Report(
                    id = "1", contentId = "c1", contentGroupId = null,
                    type = "욕설", reason = "심한 욕설을 사용하여 다른 이용자에게 불쾌감을 줬습니다",
                    status = ReportStatus.PENDING, targetUserId = "u1",
                    targetUserName = "홍길동", userId = "reporter1",
                    moderate = ModerateSeverity.HIGH,
                    createAt = System.currentTimeMillis() - 300_000,
                ),
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModerateBadgesPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ModerateSeverity.entries.forEach { ModerateBadge(severity = it) }
        }
    }
}
