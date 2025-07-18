package com.wheretogo.presentation.state

import android.net.Uri
import com.wheretogo.domain.model.map.Course
import com.wheretogo.presentation.DriveBottomSheetContent
import com.wheretogo.presentation.model.MapOverlay


data class DriveScreenState(
    val searchBarState: SearchBarState = SearchBarState(),
    val mapState: MapState = MapState(),
    val listState: ListState = ListState(),
    val popUpState: PopUpState = PopUpState(),
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val floatingButtonState: FloatingButtonState = FloatingButtonState(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    data class MapState(
        val overlayGroup: Collection<MapOverlay> = emptyList(),
        val cameraState: CameraState = CameraState(),
        val isMapReady: Boolean = false
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
        val initHeight : Int = 0,
        val content: DriveBottomSheetContent = DriveBottomSheetContent.EMPTY,
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