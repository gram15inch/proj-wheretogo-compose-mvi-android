package com.wheretogo.data.model.report

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheretogo.data.DATA_NULL
import com.wheretogo.data.DataReportType

@Entity(tableName = "LocalReport")
data class LocalReport(
    @PrimaryKey
    val reportId: String = DATA_NULL,
    val type: DataReportType = DataReportType.CHECKPOINT,
    val userId: String = DATA_NULL,
    val contentId: String = DATA_NULL,
    val targetUserId: String = "",
    val targetUserName: String = "",
    val reason: String = "",
    val status: String = "",
    val createAt: Long = 0,
    val timestamp: Long = 0
)