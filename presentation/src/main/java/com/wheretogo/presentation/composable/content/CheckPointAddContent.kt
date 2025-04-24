package com.wheretogo.presentation.composable.content

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.wheretogo.domain.model.community.ImageInfo
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.formatFileSizeToMB
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.theme.interBoldFontFamily
import com.wheretogo.presentation.theme.interFontFamily

@Composable
fun CheckPointAddContent(
    state: CheckPointAddState,
    onSubmitClick: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onImageChange: (Uri?) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = stringResource(R.string.checkpoint),
            fontSize = 16.sp,
            fontFamily = interBoldFontFamily
        )
        LocationSlider(
            modifier = Modifier.padding(8.dp),
            percentage = state.sliderPercent,
            onSliderChange = onSliderChange
        )
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            onImageChange(uri)
        }

        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = stringResource(R.string.photo),
            fontSize = 16.sp,
            fontFamily = interBoldFontFamily
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .clip(RoundedCornerShape(14.dp))
                .fillMaxWidth()
                .height(40.dp)
                .background(colorResource(R.color.gray_C7C7C7_80))
                .clickable {
                    launcher.launch("image/*")
                },
            contentAlignment = Alignment.CenterStart

        ) {
            val text =
                state.imgInfo?.let { "${it.fileName}  ${formatFileSizeToMB(it.byte)}" }
                    ?: ""
            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = text,
                fontFamily = interFontFamily
            )
        }

        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = stringResource(R.string.description),
            fontSize = 16.sp,
            fontFamily = interBoldFontFamily
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .height(100.dp)
                .background(colorResource(R.color.gray_C7C7C7_80))

        ) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clickable {
                        state.focusRequester.requestFocus()
                        keyboardController?.show()
                    },
                text = state.description,
                fontFamily = interFontFamily
            )
        }
    }

    val textColor = if (state.isSubmitActive) R.color.white else R.color.gray_848484
    val backColor = if (state.isSubmitActive) R.color.blue else R.color.white

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                color = colorResource(R.color.gray_C7C7C7_80),
                shape = RoundedCornerShape(16.dp),
                width = 1.dp
            )
            .height(60.dp)
            .background(colorResource(backColor))
            .clickable { onSubmitClick() },
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading)
            DelayLottieAnimation(
                modifier = Modifier
                    .size(50.dp),
                ltRes = R.raw.lt_loading,
                isVisible = true,
                delay = 0
            )
        else
            Text(
                text = stringResource(R.string.submit),
                color = colorResource(textColor),
                fontFamily = interBoldFontFamily
            )
    }

}

@Composable
fun LocationSlider(
    modifier: Modifier,
    percentage: Float,
    onSliderChange: (Float) -> Unit
) {
    Slider(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp),
        value = percentage,
        onValueChange = { onSliderChange(it) },
        valueRange = 0f..1f,
    )
}

@Preview
@Composable
fun CheckpointAddBottomSheetPreview() {
    val state = DriveScreenState.BottomSheetState(
        isVisible = true,
        infoState = InfoState(isRemoveButton = true),
        checkPointAddState = CheckPointAddState(
            isLoading = false, description = "안녕하세요",
            imgInfo = ImageInfo("".toUri(), "새로운 사진.jpg", 30L)
        )
    )
    Box(modifier = Modifier.width(400.dp)) {
        BottomSheet(
            modifier = Modifier.height(400.dp),
            400,
            isVisible = true,
            {},
            {},
        ) {
            if (state.isVisible) {
                CheckPointAddContent(
                    state = state.checkPointAddState,
                    {}, {}, {}
                )
            } else {
                InfoContent(
                    state = state.infoState,
                    {}, {}
                )
            }
        }
    }

}

