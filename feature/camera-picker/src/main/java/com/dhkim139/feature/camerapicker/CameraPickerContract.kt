package com.dhkim139.feature.camerapicker

import android.net.Uri
import com.dhkim139.feature.camerapicker.model.Capture
import com.dhkim139.feature.camerapicker.model.VerifiedImage
import com.dhkim139.feature.camerapicker.model.VerifiedImageGroup


data class CameraPickerState(
    val images: List<VerifiedImage> = emptyList(),
    val selectedImageId: Long? = null,
    val permission: LocationPermission = LocationPermission.DENIED,
    val locationStatus: LocationStatus = LocationStatus.Locating,
    val isEditMode: Boolean = false,
) {
    val canCapture: Boolean get() = permission == LocationPermission.GRANTED
    val canComplete: Boolean get() = images.isNotEmpty()
    val selectedImage: VerifiedImage? get() = images.firstOrNull { it.id == selectedImageId }
    val showUnverifiedBanner: Boolean get() = selectedImage?.let { !it.isVerified } == true
}

sealed interface CameraPickerIntent {
    data object CaptureClicked : CameraPickerIntent
    data class PhotoReturned(val capture: Capture?) : CameraPickerIntent
    data class SelectImage(val id: Long) : CameraPickerIntent
    data object RequestPermissionClicked : CameraPickerIntent
    data class PermissionChanged(val permission: LocationPermission) : CameraPickerIntent
    data class LocationStatusChanged(val status: LocationStatus) : CameraPickerIntent
    data object ToggleEditMode : CameraPickerIntent
    data class DeleteImage(val id: Long) : CameraPickerIntent
    data object CompleteClicked : CameraPickerIntent
    data object ExitCheck : CameraPickerIntent
    data object DialogAllow : CameraPickerIntent
}

sealed interface CameraPickerEffect {
    data class LaunchCamera(val uri: Uri) : CameraPickerEffect
    data object RequestPermission : CameraPickerEffect
    data class NavigateBackWithGroup(val group: VerifiedImageGroup) : CameraPickerEffect
    data object ShowExitDialog : CameraPickerEffect
    data object NavigateBack : CameraPickerEffect
}