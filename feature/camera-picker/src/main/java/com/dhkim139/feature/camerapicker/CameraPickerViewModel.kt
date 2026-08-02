package com.dhkim139.feature.camerapicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim139.feature.camerapicker.model.Capture
import com.dhkim139.feature.camerapicker.model.VerifiedImageGroup
import com.dhkim139.feature.camerapicker.usecase.CreateCaptureUriUseCase
import com.dhkim139.feature.camerapicker.usecase.VerifyImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LocationPermission { GRANTED, DENIED }

sealed interface LocationStatus {
    data object Locating : LocationStatus
    data class Available(val accuracyM: Float) : LocationStatus
    data object Unavailable : LocationStatus
}

@HiltViewModel
class CameraPickerViewModel @Inject constructor(
    private val createCaptureUri: CreateCaptureUriUseCase,
    private val verifyImage: VerifyImageUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CameraPickerState())
    val state: StateFlow<CameraPickerState> = _state.asStateFlow()

    private val _effect = Channel<CameraPickerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CameraPickerIntent) {
        viewModelScope.launch {
            when (intent) {
                is CameraPickerIntent.CaptureClicked -> onCapture()
                is CameraPickerIntent.PhotoReturned -> onPhotoReturned(intent.capture)
                is CameraPickerIntent.SelectImage ->
                    _state.update { it.copy(selectedImageId = intent.id) }
                is CameraPickerIntent.RequestPermissionClicked ->
                    emit(CameraPickerEffect.RequestPermission)
                is CameraPickerIntent.PermissionChanged ->
                    _state.update { it.copy(permission = intent.permission) }
                is CameraPickerIntent.LocationStatusChanged ->
                    _state.update { it.copy(locationStatus = intent.status) }
                is CameraPickerIntent.ToggleEditMode ->
                    _state.update { it.copy(isEditMode = !it.isEditMode) }
                is CameraPickerIntent.DeleteImage -> onDelete(intent.id)
                is CameraPickerIntent.CompleteClicked -> onComplete()
                is CameraPickerIntent.DialogAllow -> onDialogAllow()
                is CameraPickerIntent.ExitCheck -> onExitCheck()
            }
        }
    }

    private suspend fun onCapture() {
        if (!_state.value.canCapture) {
            emit(CameraPickerEffect.RequestPermission)
            return
        }
        val uri = createCaptureUri()
        emit(CameraPickerEffect.LaunchCamera(uri))
    }

    private fun onPhotoReturned(
        capture: Capture?
    ) {
        if (capture==null) return
        viewModelScope.launch {
            val image = verifyImage(
                capture.uri,
                capture.location,
                capture.returnedAt
            )
            _state.update {
                it.copy(
                    images = it.images + image,
                    selectedImageId = image.id,
                )
            }
        }
    }

    private fun onDelete(id: Long) {
        _state.update { s ->
            val remaining = s.images.filterNot { it.id == id }
            val nextSelected =
                if (s.selectedImageId == id) remaining.lastOrNull()?.id
                else s.selectedImageId
            s.copy(
                images = remaining,
                selectedImageId = nextSelected,
                isEditMode = remaining.isNotEmpty() && s.isEditMode,
            )
        }
    }

    private suspend fun onComplete() {
        val s = _state.value
        if (!s.canComplete) return
        emit(CameraPickerEffect.NavigateBackWithGroup(VerifiedImageGroup(s.images)))
        clear()
    }

    private suspend fun onDialogAllow() {
        emit(CameraPickerEffect.NavigateBack)
        clear()
    }

    private suspend fun onExitCheck() {
        if(_state.value.images.isNotEmpty()){
            emit(CameraPickerEffect.ShowExitDialog)
        }else {
            emit(CameraPickerEffect.NavigateBack)
            clear()
        }
    }

    private suspend fun emit(effect: CameraPickerEffect) {
        _effect.send(effect)
    }

    private fun clear(){
        _state.value = CameraPickerState()
    }
}