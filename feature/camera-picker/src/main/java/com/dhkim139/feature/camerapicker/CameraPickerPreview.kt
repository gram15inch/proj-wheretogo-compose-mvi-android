package com.dhkim139.feature.camerapicker


import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dhkim139.core.ui.theme.WhereTogoTheme
import com.dhkim139.feature.camerapicker.model.Location
import com.dhkim139.feature.camerapicker.model.VerifiedImage


private fun dummyItem(id: Long, verified: Boolean): VerifiedImage {
    return VerifiedImage(
        id = id,
        uri = Uri.EMPTY,
        isVerified = verified,
        location = Location(37.5446, 127.0559, 5f, 0L),
    )
}

fun previewImages(count: Int, unverifiedIndex: Int? = null): List<VerifiedImage> =
    (0 until count).map { i -> dummyItem(id = i.toLong(), verified = i != unverifiedIndex) }

@Composable
private fun PreviewFrame(
    state: CameraPickerState,
    locationLoading: Boolean = false,
) {
    WhereTogoTheme {
        Surface(Modifier.fillMaxSize()) {
            CameraPickerContent(
                state = state,
                locationLoading = locationLoading,
                onIntent = {},
            )
        }
    }
}

private fun grantedState(
    images: List<VerifiedImage> = emptyList(),
    selectedId: Long? = images.lastOrNull()?.id,
    editMode: Boolean = false,
) = CameraPickerState(
    images = images,
    selectedImageId = selectedId,
    isEditMode = editMode,
    permission = LocationPermission.GRANTED,
    locationStatus = LocationStatus.Available(5f),
)

const val heightDp = 850

@Preview(name = "1. 준비 (사진 없음)", showBackground = true, heightDp = heightDp)
@Composable
private fun Preview_Empty() = PreviewFrame(grantedState())

@Preview(name = "2. 세션 (전부 인증)", showBackground = true, heightDp = heightDp)
@Composable
private fun Preview_Session() = PreviewFrame(grantedState(previewImages(3)))

@Preview(name = "3. 미인증 선택 (배너)", showBackground = true, heightDp = heightDp)
@Composable
private fun Preview_UnverifiedSelected() {
    val images = previewImages(4, unverifiedIndex = 3)
    PreviewFrame(grantedState(images, selectedId = images.last().id))
}

@Preview(name = "4. 편집 모드", showBackground = true, heightDp = heightDp)
@Composable
private fun Preview_EditMode() = PreviewFrame(grantedState(previewImages(3), editMode = true))

@Preview(name = "5. 촬영 중 (위치 찾는 중)", showBackground = true, heightDp = heightDp)
@Composable
private fun Preview_CaptureInProgress() =
    PreviewFrame(
        state = grantedState(previewImages(3)).copy(
            locationStatus = LocationStatus.Locating,
        ),
        locationLoading = true,
    )


@Preview(name = "6. 권한 없음", showBackground = true, heightDp = heightDp)
@Composable
private fun Preview_NoPermission() = PreviewFrame(
    CameraPickerState(
        permission = LocationPermission.DENIED,
        locationStatus = LocationStatus.Unavailable,
    )
)