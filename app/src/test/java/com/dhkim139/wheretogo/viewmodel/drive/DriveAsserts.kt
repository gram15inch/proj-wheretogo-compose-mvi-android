package com.dhkim139.wheretogo.viewmodel.drive

import com.dhkim139.wheretogo.feature.FlowAssertions
import com.google.common.truth.Truth.assertThat
import com.wheretogo.presentation.DriveFloatingVisibleMode
import com.wheretogo.presentation.DriveVisibleMode
import com.wheretogo.presentation.event.DriveEvent
import com.wheretogo.presentation.state.DriveScreenState

// Assert : 코스 세부 상태로 변경
suspend fun FlowAssertions<DriveScreenState, DriveEvent>.assertCourseDetail() {
    state.awaitItem().run {
        assertThat(stateMode).isEqualTo(DriveVisibleMode.CourseDetail)
        assertThat(floatingButtonState.stateMode).isEqualTo(DriveFloatingVisibleMode.Default)
    }
}

// Assert : 체크포인트 세부 상태로 변경
suspend fun FlowAssertions<DriveScreenState, DriveEvent>.assertCheckPointPopup() {
    state.awaitItem().run {
        assertThat(stateMode).isEqualTo(DriveVisibleMode.BlurCheckpointDetail)
        assertThat(floatingButtonState.stateMode).isEqualTo(DriveFloatingVisibleMode.Popup)
    }
}
