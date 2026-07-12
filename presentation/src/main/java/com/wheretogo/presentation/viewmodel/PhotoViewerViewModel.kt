package com.wheretogo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheretogo.domain.ZOOM
import com.wheretogo.domain.feature.LocationService
import com.wheretogo.domain.handler.ErrorHandler
import com.wheretogo.domain.model.checkpoint.CheckPointContent
import com.wheretogo.domain.model.gallery.GalleryPhoto
import com.wheretogo.domain.usecase.checkpoint.AddCheckpointToCourseUseCase
import com.wheretogo.domain.usecase.checkpoint.RemoveCheckPointUseCase
import com.wheretogo.domain.usecase.course.GetCourseUseCase
import com.wheretogo.domain.usecase.gallery.GetStampUseCase
import com.wheretogo.domain.model.gallery.Stamp
import com.wheretogo.domain.model.map.CameraState
import com.wheretogo.domain.model.map.MoveAnimation
import com.wheretogo.presentation.AppLifecycle
import com.wheretogo.presentation.MainDispatcher
import com.wheretogo.presentation.composable.photoviewer.StampState
import com.wheretogo.presentation.feature.map.MapOverlayService
import com.wheretogo.presentation.intent.PhotoViewerIntent
import com.wheretogo.presentation.model.CameraOption
import com.wheretogo.presentation.state.PhotoViewerState
import com.wheretogo.presentation.state.StampProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject



@HiltViewModel
class PhotoViewerViewModel @Inject constructor(
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val handler: ErrorHandler,
    private val getCourseUseCase: GetCourseUseCase,
    private val getStampUseCase: GetStampUseCase,
    private val addCheckpointToCourseUseCase: AddCheckpointToCourseUseCase,
    private val removeCheckPointUseCase: RemoveCheckPointUseCase,
    private val mapOverlayService: MapOverlayService,
    private val locationService: LocationService
) : ViewModel() {
    private val _state = MutableStateFlow(
        PhotoViewerState(stampState = StampState.Loading)
    )
    val state: StateFlow<PhotoViewerState> = _state

    private var _stamp = MutableStateFlow<Stamp?>(null)
    private var _event = MutableSharedFlow<MapEvent>()
    var event : SharedFlow<MapEvent> = _event

    private var _latestCamera = MutableStateFlow<CameraState?>(null)

    val overlay = mapOverlayService.overlays
    val fingerPrint = mapOverlayService.fingerPrintFlow

    fun handleIntent(intent: PhotoViewerIntent) {
        viewModelScope.launch(dispatcher) {
            when (intent) {
                is PhotoViewerIntent.Refresh -> refreshViewer(intent.photo)
                is PhotoViewerIntent.Stamp -> stamp(intent.photo, intent.description)
                is PhotoViewerIntent.RemoveStamp -> removeStamp(intent.photo)
                is PhotoViewerIntent.CameraUpdate -> cameraUpdate(intent.camera)
                is PhotoViewerIntent.LifecycleChange -> lifecycleChange(intent.event)
            }
        }
    }

    private fun cameraUpdate(camera: CameraState) {
        _latestCamera.value = camera
    }

    suspend fun handleError(e: Throwable) {
        handler.handle(e)
    }

    private suspend fun refreshViewer(photo: GalleryPhoto) {
        val latestCamera = _latestCamera.value
        val option = CameraOption.fromGalleryPhoto(photo)
            ?.copy(zoom = ZOOM.DISTRICT.level, moveAnimation = MoveAnimation.APP_EASING)
        val courseId = photo.courseId

        if(option != null){
            mapOverlayService.refreshSpot(option.latLng)
            if (latestCamera != null) {
                if (!locationService.isContainByViewPort(
                        vp = latestCamera.viewport, target = option.latLng
                    )
                ) {
                    _event.emit(MapEvent.MoveCamera(option))
                }
            }
        }

        if (courseId != null) {
            withContext(Dispatchers.IO) {
                getCourseUseCase(courseId)
            }.onSuccess { course ->
                mapOverlayService.refreshPath(course)
            }

            withContext(Dispatchers.IO) {
                getStampUseCase(courseId)
            }.onSuccess { stamp ->
                if (stamp == null) {
                    _stamp.update { null }
                    _state.update {
                        it.copy(
                            stampProgress = StampProgress.Idle,
                            stampState = StampState.Empty
                        )
                    }
                } else {
                    _stamp.update { stamp.copy(pickerId = photo.id) }
                    _state.update {
                        it.copy(
                            stampProgress = StampProgress.Idle,
                            stampState = StampState.Stamped(
                                stampedAt = stamp.createAt,
                                thumbnail = stamp.thumbnail,
                                description = stamp.description
                            )
                        )
                    }
                }
            }.onFailure {
                _state.update { it.copy(stampProgress = StampProgress.Idle) }
                handleError(it)
            }
        }
    }

    private suspend fun stamp(photo: GalleryPhoto, description: String?) {
        val courseId = photo.courseId
        val latlng = photo.exif.location
        when {
            _state.value.stampProgress != StampProgress.Idle -> return
            courseId == null -> return
            latlng == null -> return
        }
        _state.update {
            it.copy(stampProgress = StampProgress.Stamping)
        }

        withContext(Dispatchers.IO) {
            addCheckpointToCourseUseCase.stamp(
                CheckPointContent(
                    groupId = courseId,
                    imageId = photo.imageId,
                    latLng = latlng,
                    description = description ?: ""
                )
            )
        }.onSuccess {
            refreshViewer(photo)
        }.onFailure {
            _state.update { it.copy(stampProgress = StampProgress.Idle) }
            handleError(it)
        }
    }

    private suspend fun removeStamp(photo: GalleryPhoto) {
        if (_state.value.stampProgress != StampProgress.Idle) return
        val stamp = _stamp.value ?: return
        _state.update { it.copy(stampProgress = StampProgress.Removing) }

        withContext(Dispatchers.IO) {
            removeCheckPointUseCase.unStamp(stamp.id)
        }.onSuccess {
            refreshViewer(photo)
        }.onFailure {
            _state.update { it.copy(stampProgress = StampProgress.Idle) }
            handleError(it)
        }
    }

    private fun lifecycleChange(event: AppLifecycle) {
        when(event){
            AppLifecycle.onDispose -> {_latestCamera.value = null }
            else -> {}
        }
    }
}