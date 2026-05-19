package com.wheretogo.presentation.event

import com.wheretogo.domain.model.checkpoint.CheckPoint
import com.wheretogo.domain.model.course.Course
import com.wheretogo.domain.model.map.CameraMoveTrigger
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.domain.model.map.ContentOperation
import com.wheretogo.domain.model.map.MarkerInfo
import com.wheretogo.domain.model.map.MoveCameraOption
import com.wheretogo.domain.model.map.OverlayOperation
import com.wheretogo.domain.model.map.RefreshContentOption
import com.wheretogo.domain.model.map.RefreshOverlayOption
import com.wheretogo.domain.repository.DefaultMapId
import kotlinx.coroutines.flow.MutableSharedFlow

sealed class DriveEvent {
    data class MoveCamera(val option: MoveCameraOption): DriveEvent()
    data class RefreshContent(val option: RefreshContentOption) : DriveEvent()
    data class RefreshOverlay(val option: RefreshOverlayOption) : DriveEvent()
    data class Focus(val course: Course) : DriveEvent()
    data object Release : DriveEvent()
    data object ClearMap : DriveEvent()

    companion object {
        suspend fun MutableSharedFlow<DriveEvent>.refreshCourses(cameraState: CameraState? = null) {
            emit(
                RefreshContent(
                    RefreshContentOption(
                        ContentOperation.REFRESH_COURSES,
                        cameraState = cameraState // null 일경우 최근 위치
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.refreshCluster(clusterId: String) {
            emit(
                RefreshContent(
                    RefreshContentOption(ContentOperation.REFRESH_CLUSTER, id = clusterId)
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.addMarker(markerInfo: MarkerInfo) {
            emit(
                RefreshOverlay(
                    RefreshOverlayOption(
                        OverlayOperation.CREATE,
                        markerInfo = markerInfo
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.updateMarker(markerInfo: MarkerInfo) {
            emit(
                RefreshOverlay(
                    RefreshOverlayOption(
                        OverlayOperation.UPDATE,
                        markerInfo = markerInfo,
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.deleteMarker(id: String) {
            emit(
                RefreshOverlay(
                    RefreshOverlayOption(
                        OverlayOperation.DELETE,
                        id
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.addLeaf(checkPoint: CheckPoint) {
            emit(
                RefreshOverlay(
                    RefreshOverlayOption(
                        OverlayOperation.CREATE,
                        checkPoint = checkPoint
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.deleteLeaf(
            checkpointId: String,
            coursedId: String = ""
        ) {
            emit(
                RefreshOverlay(
                    RefreshOverlayOption(
                        OverlayOperation.DELETE,
                        courseId = coursedId,
                        checkPointId = checkpointId
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.deleteSelectCourse() {
            emit(
                RefreshContent(
                    RefreshContentOption(
                        ContentOperation.DELETE_COURSE,
                        id = DefaultMapId.SELECT_COURSE_ID.name
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.deleteSelectCheckPoint() {
            emit(
                RefreshContent(
                    RefreshContentOption(
                        ContentOperation.DELETE_CHECKPOINT,
                        id = DefaultMapId.SELECT_CHECKPOINT_ID.name,
                        groupId = DefaultMapId.SELECT_COURSE_ID.name
                    )
                )
            )
        }

        suspend fun MutableSharedFlow<DriveEvent>.stopCameraWhenSheetChange(){//
            emit(
                MoveCamera(
                    MoveCameraOption(
                        trigger = CameraMoveTrigger.BOTTOM_SHEET_DOWN
                    )
                )
            )
        }

    }
}