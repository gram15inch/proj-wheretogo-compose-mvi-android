package com.wheretogo.data.model.report

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheretogo.data.DATA_NULL

@Entity(tableName = "LocalReport")
data class LocalReport(
    @PrimaryKey
    val reportId: String = DATA_NULL,
    val type: String = DATA_NULL,
    val userId: String = DATA_NULL,
    val contentId: String = DATA_NULL,
    val targetUserId: String = "",
    val targetUserName: String = "",
    val reason: String = "",
    val status: String = "",
    val timestamp : Long = 0
)