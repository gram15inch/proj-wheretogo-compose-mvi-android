package com.wheretogo.presentation.composable.content

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheretogo.presentation.R
import com.wheretogo.presentation.feature.consumptionEvent
import com.wheretogo.presentation.feature.formatFileSizeToMB
import com.wheretogo.presentation.state.CheckPointAddState
import com.wheretogo.presentation.state.DriveScreenState
import com.wheretogo.presentation.state.InfoState
import com.wheretogo.presentation.theme.interBoldFontFamily

@Composable
fun DriveBottomSheet(
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    onBottomSheetClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    SlideAnimation(
        modifier = modifier,
        visible = isVisible,
        direction = AnimationDirection.CenterDown
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .fillMaxWidth()
                .consumptionEvent()
                .statusBarsPadding()
                .background(Color.White)

        ) {
            Column {
                DragHandle(modifier = Modifier.clickable { onBottomSheetClose() })
                content()
            }
        }
    }
}

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
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.checkpoint),
            fontSize = 16.sp,
            fontFamily = interBoldFontFamily
        )
        LocationSlider(
            percentage = state.sliderPercent,
            onSliderChange = onSliderChange
        )
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            onImageChange(uri)
        }

        Text(
            text = stringResource(R.string.photo),
            fontSize = 16.sp,
            fontFamily = interBoldFontFamily
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
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
            Text(modifier = Modifier.padding(start = 10.dp), text = text)
        }

        Text(
            text = stringResource(R.string.description),
            fontSize = 16.sp,
            fontFamily = interBoldFontFamily
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .height(100.dp)
                .background(colorResource(R.color.gray_C7C7C7_80))

        ) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .clickable {
                        state.focusRequester.requestFocus()
                        keyboardController?.show()
                    },
                text = state.description
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .height(60.dp)
            .background(colorResource(R.color.blue))
            .clickable { onSubmitClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.submit))
    }

}

@Composable
fun InfoContent(
    state: InfoState,
    onReportClick: (InfoState) -> Unit,
    onRemoveClick: (InfoState) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.isRemove)
                    CircleButton(R.drawable.ic_delete, onClick = {
                        onRemoveClick(state)
                    })
                CircleButton(R.drawable.ic_block, onClick = {
                    onReportClick(state)
                })
            }
        }
    }
}

@Composable
private fun CircleButton(icon: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .border(width = 1.dp, shape = CircleShape, color = Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(icon), contentDescription = "")
    }
}

@Composable
fun LocationSlider(
    percentage: Float,
    onSliderChange: (Float) -> Unit
) {
    Slider(
        modifier = Modifier.fillMaxWidth(),
        value = percentage,
        onValueChange = { onSliderChange(it) },
        valueRange = 0f..1f,
    )
}

@Preview
@Composable
fun CheckpointAddBottomSheetPreview() {
    val state = DriveScreenState.BottomSheetState(
        isVisible = false,
        infoState = InfoState(isRemove = true)
    )
    Box(modifier = Modifier.width(400.dp)) {
        DriveBottomSheet(
            modifier = Modifier,
            isVisible = true,
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



