package com.wheretogo.presentation.state

import com.wheretogo.presentation.composable.photoviewer.StampState

enum class StampProgress { Idle, Stamping, Removing }

data class PhotoViewerState(
    val stampState: StampState = StampState.Empty,
    val stampProgress: StampProgress = StampProgress.Idle
)