package com.wheretogo.presentation.state

import android.net.Uri
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.model.MapOverlay


data class DriveScreenState(
    val mapState: MapState = MapState(),
    val listState: ListState = ListState(),
    val popUpState: PopUpState = PopUpState(),
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val floatingButtonState: FloatingButtonState = FloatingButtonState(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    data class MapState(
        val isMapReady: Boolean = false,
        val cameraState: CameraState = CameraState(),
        val mapOverlayGroup: Set<MapOverlay> = emptySet()
    )

    data class ListState(
        val isVisible: Boolean = true,
        val listItemGroup: List<ListItemState> = emptyList(),
        val clickItem: ListItemState = ListItemState()
    ) {
        data class ListItemState(
            val distanceFromCenter: Int = 0,
            val isBookmark: Boolean = false,
            val course: Course = Course()
        )
    }

    data class PopUpState(
        val isVisible: Boolean = false,
        val checkPointId: String = "",
        val imageUri: Uri? = null,
        val commentState: CommentState = CommentState()
    )

    data class BottomSheetState(
        val isVisible: Boolean = false,
        val isCheckPointAdd: Boolean = true,
        val checkPointAddState: CheckPointAddState = CheckPointAddState(),
        val infoState: InfoState = InfoState()
    )

    data class FloatingButtonState(
        val isCommentVisible: Boolean = false,
        val isCheckpointAddVisible: Boolean = false,
        val isInfoVisible: Boolean = false,
        val isExportVisible: Boolean = false,
        val isBackPlateVisible: Boolean = false,
        val isFoldVisible: Boolean = false
    )
}