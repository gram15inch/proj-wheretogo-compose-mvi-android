package com.dhkim139.admin.wheretogo.feature.report.model

data class Report(
    val id: String,
    val contentId: String,
    val contentGroupId: String?,
    val type: String,
    val reason: String,
    val status: ReportStatus,
    val targetUserId: String,
    val targetUserName: String,
    val userId: String,
    val moderate: ModerateSeverity,
    val createAt: Long,
)

enum class ReportStatus(val raw: String) {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun from(value: String) = entries.firstOrNull { it.raw == value } ?: UNKNOWN
    }

    fun toDisplayName() = when (this) {
        PENDING   -> "미처리"
        APPROVED  -> "승인"
        REJECTED -> "반려"
        UNKNOWN   -> "알 수 없음"
    }
}

enum class ModerateSeverity(val raw: String) {
    SAFE("SAFE"),
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun from(value: String) = entries.firstOrNull { it.raw == value } ?: UNKNOWN
    }

    fun toDisplayName() = when (this) {
        HIGH    -> "위험"
        MEDIUM  -> "주의"
        LOW     -> "낮음"
        SAFE    -> "안전"
        UNKNOWN -> "알 수 없음"
    }
}

