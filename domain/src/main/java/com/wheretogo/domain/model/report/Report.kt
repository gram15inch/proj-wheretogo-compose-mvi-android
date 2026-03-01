package com.wheretogo.domain.model.report

data class Report(
    val reportId: String = "",
    val contentId: String = "",
    val contentGroupId: String = "",
    val type: ReportType,
    val reason: ReportReason,
    val status: ReportStatus,
    val userId: String = "",
    val targetUserId: String = "",
    val targetUserName: String = "",
    val moderate: String = "",
    val createAt: Long = 0L
)

enum class ReportType {
    USER, COURSE, COMMENT, CHECKPOINT;

    companion object{
        fun parseString(string: String): ReportType {
            return runCatching {
                ReportType.valueOf(string)
            }.getOrDefault(COMMENT)
        }
    }
}

enum class ReportReason {
    SPAM,
    HARASSMENT,
    INAPPROPRIATE,
    VIOLENCE,
    HATE_SPEECH,
    MISINFORMATION,
    COPYRIGHT,
    OTHER;

    companion object{
        fun parseString(string: String): ReportReason {
            return runCatching {
                ReportReason.valueOf(string)
            }.getOrDefault(SPAM)
        }
    }
}

enum class ReportStatus {
    PENDING,    // 보류
    APPROVED,   // 승인
    REJECTED;    // 반려

    companion object{
        fun parseString(string:String):ReportStatus{
            return runCatching {
                ReportStatus.valueOf(string)
            }.getOrDefault(PENDING)
        }
    }
}
