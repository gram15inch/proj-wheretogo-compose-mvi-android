package com.wheretogo.presentation.state

import com.wheretogo.presentation.model.AdItem

data class FloatingButtonState(
    val adItemGroup: List<AdItem> = emptyList(),
    val isCommentVisible: Boolean = false,
    val isCheckpointAddVisible: Boolean = false,
    val isInfoVisible: Boolean = false,
    val isExportVisible: Boolean = false,
    val isBackPlateVisible: Boolean = false,
    val isFoldVisible: Boolean = false
)